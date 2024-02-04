package datacarton

import collections.annotatedtargets.*
import datacarton.annotations.Export
import datacarton.processors.DataPackageProcessor
import datacarton.annotations.Import
import datacarton.annotations.Pack
import datacarton.processors.PackageProcessor
import datacarton.processors.PublicationProcessor
import datacarton.processors.TelemetryPublicationProcessor
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LateInitCell
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.lang.annotation.Inherited
import java.lang.reflect.AccessibleObject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier
import kotlin.collections.HashMap

object DataCarton : Feature {
	private var autoUpdate by LateInitCell<Boolean>()
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.includesExactlyOneOf(DairyCore::class.java, Attach::class.java)
			.bindOutputTo {
				autoUpdate = when (it) {
					is Attach -> {
						it.autoUpdate
					}
					else -> {
						true
					}
				}
			}

	/**
	 * sets up data carton with the default package processor
	 */
	fun initWithDefaultPackageProcessors(defaultRenderOrder: RenderOrder = RenderOrder.DEFAULT_MAPPING, vararg additionalPublicationProcessors: PublicationProcessor) {
		this.defaultRenderOrder = defaultRenderOrder
		packageProcessors.add(DataPackageProcessor)
		publicationProcessors.addAll(additionalPublicationProcessors)
	}

	/**
	 * sets up data carton with the default package processor
	 */
	fun initWithDefaultPackageProcessors(vararg additionalPublicationProcessors: PublicationProcessor) {
		initWithDefaultPackageProcessors(RenderOrder.DEFAULT_MAPPING, *additionalPublicationProcessors)
	}

	/**
	 * sets up DataCarton with a telemetry output
	 */
	fun initFromTelemetry(telemetry: Telemetry, defaultRenderOrder: RenderOrder = RenderOrder.DEFAULT_MAPPING, vararg additionalPublicationProcessors: PublicationProcessor) {
		this.defaultRenderOrder = defaultRenderOrder
		packageProcessors.add(DataPackageProcessor)
		publicationProcessors.add(TelemetryPublicationProcessor(telemetry))
		publicationProcessors.addAll(additionalPublicationProcessors)
	}

	override fun preUserInitHook(opMode: Wrapper) {
		startTime = System.nanoTime()
	}

	override fun postUserInitHook(opMode: Wrapper) {
		if (publicationProcessors.isEmpty()) throw IllegalStateException("DataCarton had no attached publication processors")
		if (autoUpdate) {
			update()
		}
	}

	override fun postUserInitLoopHook(opMode: Wrapper) {
		if (autoUpdate) {
			update()
		}
	}

	override fun postUserStartHook(opMode: Wrapper) {
		if (autoUpdate) {
			update()
		}
	}

	override fun postUserLoopHook(opMode: Wrapper) {
		if (autoUpdate) {
			update()
		}
	}

	override fun postUserStopHook(opMode: Wrapper) {
		if (autoUpdate) {
			update()
		}
		defaultRenderOrder = RenderOrder.DEFAULT_MAPPING
		publicationProcessors.clear()
		packageProcessors.clear()
		rendererHashMap.clear()
		settingsMap.clear()
		configMap.clear()
	}

	var defaultRenderOrder: RenderOrder = RenderOrder.DEFAULT_MAPPING
	val publicationProcessors: MutableSet<PublicationProcessor> = mutableSetOf()
	val packageProcessors: MutableSet<PackageProcessor> = mutableSetOf()

	private val rendererHashMap: HashMap<String, CartonComponentRenderer> = HashMap()

	private var startTime: Long = System.nanoTime()
	private val settingsMap = HashMap<String, RenderOrder>()

	private val thread = Thread {
		while (true) {
			if (requestUpdate.get()) {
				publicationProcessors.forEach {
					if (it.ignoreUpdate()) return@forEach
					it.initPublication()
					rendererHashMap.forEach { (_, component) ->
						it.accept(component)
					}
					it.updatePublication()
				}
				requestUpdate.set(false)
			}
		}
	}

	private val requestUpdate = AtomicBoolean(false)

	/**
	 * returns if the update request succeeded or not
	 */
	fun update(): Boolean {
		// if the thread wasn't started yet, start it
		if (!thread.isAlive) thread.start()

		// an update is currently underway
		if (requestUpdate.get()) return false

		// else, request one
		requestUpdate.set(true)
		return true
	}

	/**
	 * blocking until an update is completed, should probably not be used except for testing
	 */
	fun awaitUpdate() {
		var firstAttempt = true
		while (firstAttempt or !update()) {
			firstAttempt = false
		}
	}

	/**
	 * packages data from the root
	 *
	 * @param root the root object to start searching from
	 */
	fun packageData(root: Any) {
		if (packageProcessors.isEmpty()) throw IllegalStateException("DataCarton had no attached package processors")

		configMap.forEach {
			settingsMap[it.key] = RenderOrder(it.value.renders)
		}

		val builderMap: MutableMap<String, CartonComponentRenderer.Builder> = HashMap()
		val packages: List<Packaged> = explorePackageTree({ root }, root.javaClass, root.javaClass.simpleName, false)

		packageProcessors.forEach { processor ->
			run {
				processor.process({ root }, root.javaClass.simpleName, null)
						.forEach { processedOutput ->
							builderMap.putIfAbsent(processedOutput.group, CartonComponentRenderer.Builder(settingsMap[processedOutput.group]
									?: defaultRenderOrder))
							builderMap[processedOutput.group]?.add(processedOutput.cartonComponentClass, processedOutput.dataLine)
						}
			}
		}

		for (packedField in packages) {
			packageProcessors.forEach { processor ->
				processor.process(packedField.childInstance, packedField.group, packedField as? ImportingPackaged)
						.forEach { processedOutput ->
							builderMap.putIfAbsent(processedOutput.group, CartonComponentRenderer.Builder(settingsMap[processedOutput.group]
									?: defaultRenderOrder))
							builderMap[processedOutput.group]?.add(processedOutput.cartonComponentClass, processedOutput.dataLine)
						}
			}
		}

		// appends the unbuilt settings
		settingsMap.forEach {
			builderMap.putIfAbsent(it.key, CartonComponentRenderer.Builder(it.value))
		}

		// builds the renders
		for ((key, value) in builderMap) {
			rendererHashMap[key] = value.build(key)
		}
	}

	/**
	 * recursively finds the fields which should be packaged in the opmode runtime tree
	 *
	 * @param root root object
	 * @param targetClass      class of the root object
	 */
	private fun explorePackageTree(root: Supplier<*>, targetClass: Class<*>, parentGroup: String, parentBundled: Boolean): List<Packaged> {
		val packages = analyseAccessibleObjects(
				(listOf(*targetClass.declaredMethods, *targetClass.declaredFields) as List<AccessibleObject>),
				root, parentGroup, parentBundled
		)
		val recursivePackages = ArrayList<Packaged>()
		val parent = targetClass.superclass
		if (parent != null && parent != Any::class.java) {
			recursivePackages.addAll(explorePackageTree(root, parent, parentGroup, parentBundled))
		}
		for (packaged in packages) {
			if (packaged.childInstance.get() != null) {
				if (packaged is ImportingPackaged) {
					recursivePackages.addAll(
							analyseAccessibleObjects(
									packaged.packMethods + packaged.packFields,
									root, parentGroup, packaged.bundle
							)
					)
				}
				recursivePackages.addAll(
						explorePackageTree(
								packaged.childInstance,
								packaged.childInstance.get()!!.javaClass,
								if (packaged.bundle) parentGroup else packaged.group,
								packaged.bundle
						)
				)
			}
		}
		packages.addAll(recursivePackages)
		return packages
	}

	private fun analyseAccessibleObjects(accessibleObjects: List<AccessibleObject>, root: Supplier<*>, parentGroup: String, parentBundled: Boolean) : MutableList<Packaged> {
		val result = mutableListOf<Packaged>()
		accessibleObjects.stream()
				.forEach {
					if (parentBundled && it.getAnnotation(Export::class.java)?.bundle != true) return@forEach

					if (it.isAnnotationPresent(Pack::class.java)) {
						it.isAccessible = true

						val bundled = it.getAnnotation(Pack::class.java)?.bundle ?: false
						if (it.isAnnotationPresent(Import::class.java)) {
							result.add(ImportingPackaged(root, it, if(bundled) parentGroup else null))
						}
						else {
							result.add(Packaged(root, it, if(bundled) parentGroup else null))
						}
					}
				}
		return result
	}

	private val configMap = mutableMapOf<String, DataConfiguration>()
	fun configureFor(group: String) : DataConfiguration {
		configMap[group] = DataConfiguration()
		return configMap[group]!!
	}
	class DataConfiguration {
		internal val renders = arrayListOf<Render<*>>()
		fun <T> with(render: Render<T>): DataConfiguration {
			renders.add(render)
			return this
		}

		fun with(renderOrder: RenderOrder): DataConfiguration {
			renders.addAll(renderOrder.renders)
			return this
		}

		fun configureFor(group: String) : DataConfiguration {
			configMap[group] = DataConfiguration()
			return configMap[group]!!
		}
	}

	fun publishMessage(group: String, label: String, contents: Any) {
		val renderer = rendererHashMap[group] ?: return
		renderer.add(MessageBoard::class.java, TimedDataLine(startTime, label, contents.toString()))
	}

	fun publishMessage(group: String, contents: Any) {
		publishMessage(group, "", contents)
	}

	@Retention(AnnotationRetention.RUNTIME)
	@Target(AnnotationTarget.CLASS)
	@MustBeDocumented
	@Inherited
	annotation class Attach(
			/**
			 * if [DataCarton] should automatically call [DataCarton.update] after user code
			 */
			val autoUpdate: Boolean = true
	)
}
