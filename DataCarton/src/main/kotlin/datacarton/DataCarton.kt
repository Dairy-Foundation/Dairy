package datacarton

import collections.annotatedtargets.*
import datacarton.annotations.DataPackageProcessor
import datacarton.annotations.Flatpack
import datacarton.annotations.Pack
import datacarton.annotations.PackageProcessor
import datacarton.annotations.PublicationProcessor
import datacarton.annotations.TelemetryPublicationProcessor
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.Dependency
import dev.frozenmilk.dairy.core.DependencySet
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.OpModeWrapper
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.lang.reflect.AccessibleObject
import java.util.function.Supplier
import kotlin.collections.HashMap

object DataCarton : Feature {
	override val dependencies: Set<Dependency<*>> = DependencySet(this)
			.includesExactlyOneOf(DairyCore::class.java, DairyCore.DataCarton::class.java)

	fun initFromTelemetry(telemetry: Telemetry, defaultRenderOrder: RenderOrder, vararg additionalPublicationProcessors: PublicationProcessor) {
		telemetry.isAutoClear = false
		telemetry.clearAll()
		telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE)
		telemetry.captionValueSeparator = ""
		telemetry.itemSeparator = ""
		this.defaultRenderOrder = defaultRenderOrder
		packageProcessors.add(DataPackageProcessor)
		publicationProcessors.add(TelemetryPublicationProcessor(telemetry.addLine()))
		publicationProcessors.addAll(additionalPublicationProcessors)
	}

	fun initFromTelemetry(telemetry: Telemetry, defaultRenderOrder: RenderOrder = RenderOrder.DEFAULT_MAPPING) {
		telemetry.isAutoClear = false
		telemetry.clearAll()
		telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE)
		telemetry.captionValueSeparator = ""
		telemetry.itemSeparator = ""
		this.defaultRenderOrder = defaultRenderOrder
		packageProcessors.add(DataPackageProcessor)
		publicationProcessors.add(TelemetryPublicationProcessor(telemetry.addLine()))
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {
		startTime = System.nanoTime()
	}

	override fun postUserInitHook(opMode: OpModeWrapper) {
		if (packageProcessors.isEmpty()) throw IllegalStateException("DataCarton had no attached package processors")
		if (publicationProcessors.isEmpty()) throw IllegalStateException("DataCarton had no attached publication processors")
		update()
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
	}

	override fun postUserStartHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {
		update()
		defaultRenderOrder = RenderOrder.DEFAULT_MAPPING
		publicationProcessors.clear()
		packageProcessors.clear()
		rendererHashMap.clear()
		settingsMap.clear()
	}

	var defaultRenderOrder: RenderOrder = RenderOrder.DEFAULT_MAPPING
	val publicationProcessors: MutableSet<PublicationProcessor> = mutableSetOf()
	val packageProcessors: MutableSet<PackageProcessor> = mutableSetOf()
	private val rendererHashMap: HashMap<String, CartonComponentRenderer> = HashMap()

	//    private lateinit var instanceGroups: WeakHashMap<Any, Pair<Boolean, String>>
	private var startTime: Long = System.nanoTime()
	private val settingsMap = HashMap<String, RenderOrder>()

//	constructor(telemetry: Telemetry, renderOrder: RenderOrder) {
//	}

	fun update() {
		publicationProcessors.forEach {
			it.initPublication()
		}
		publicationProcessors.forEach {
			rendererHashMap.forEach { (_, component) ->
				it.accept(component)
			}
		}
		publicationProcessors.forEach {
			it.updatePublication()
		}
//		val builder = StringBuilder()
//		for (traceComponent in traceComponents.values) {
//			builder.append(traceComponent)
//			builder.append("\n")
//		}
//		set(builder.toString())
	}

	/**
	 * packages data from the root
	 *
	 * @param root the root object to start searching from
	 */
	fun packageData(root: GroupedData) {
		val builderMap: MutableMap<String, CartonComponentRenderer.Builder> = HashMap()
//        instanceGroups = WeakHashMap();
		val packages: List<Packaged> = tracedFields({ root }, root.javaClass, root.group)

		packageProcessors.forEach { processor ->
			run {
				processor.process({ root }, root.group, null)
						.forEach { processedOutput ->
							builderMap.putIfAbsent(processedOutput.group, CartonComponentRenderer.Builder(settingsMap[processedOutput.group]
									?: defaultRenderOrder))
							builderMap[processedOutput.group]?.add(processedOutput.cartonComponentClass, processedOutput.dataLine)
						}
			}
		}

		for (packedField in packages) {
			packageProcessors.forEach { processor ->
				processor.process(packedField.childInstance, packedField.group, packedField as? Flatpackaged)
						.forEach { processedOutput ->
							builderMap.putIfAbsent(processedOutput.group, CartonComponentRenderer.Builder(settingsMap[processedOutput.group]
									?: defaultRenderOrder))
							builderMap[processedOutput.group]?.add(processedOutput.cartonComponentClass, processedOutput.dataLine)
						}
			}
		}

		// builds the renders
		for ((key, value) in builderMap) {
			rendererHashMap[key] = value.build(key)
		}
	}

	/**
	 * recursively finds the fields which contain traces in the opmode runtime tree
	 *
	 * @param root root object
	 * @param targetClass      class of the root object
	 */
	private fun tracedFields(root: Supplier<*>, targetClass: Class<*>, parentGroup: String): ArrayList<Packaged> {
		val traceMap = ArrayList<Packaged>()
		(listOf(*targetClass.declaredMethods, *targetClass.declaredFields) as List<AccessibleObject>).stream()
				.forEach {
					if (it.isAnnotationPresent(Pack::class.java)) {
						it.isAccessible = true
						if (it.isAnnotationPresent(Flatpack::class.java)) {
							traceMap.add(Flatpackaged(root, it, parentGroup))
//                            instanceGroups.putIfAbsent(traceMap.last().childInstance.get()!!, Pair(true, traceMap.last().group));
						} else {
							traceMap.add(Packaged(root, it))
//                            instanceGroups.putIfAbsent(traceMap.last().childInstance.get()!!, Pair(false, traceMap.last().group));
						}
					}
				}
		val rTraceMap = ArrayList<Packaged>()
		val parent = targetClass.superclass
		if (parent != null && parent != Any::class.java) {
			rTraceMap.addAll(tracedFields(root, parent, parentGroup))
		}
		for (tracedField in traceMap) {
			if (tracedField is Flatpackaged) {
				rTraceMap.addAll(
						tracedFields(
								tracedField.childInstance,
								tracedField.childInstance.get()!!.javaClass,
								parentGroup
						)
				)
			} else {
				rTraceMap.addAll(
						tracedFields(
								tracedField.childInstance,
								tracedField.childInstance.get()!!.javaClass,
								tracedField.group
						)
				)
			}
		}
		traceMap.addAll(rTraceMap)
		return traceMap
	}

	//    companion object {
//        @Contract(pure = true)
//        fun getDataFields(bloodstream: Any): List<AnnotatedDataField> {
//            val fields = ArrayList<Field>()
//            var searchTargetClass: Class<*>? = bloodstream.javaClass
//            val group = bloodstream.javaClass.getAnnotation(Pack::class.java)?.group
//                    ?: bloodstream.javaClass.simpleName
//            while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//                fields.addAll(listOf(*searchTargetClass.declaredFields))
//                searchTargetClass = searchTargetClass.superclass
//            }
//            return fields.stream()
//                    .filter { f: Field ->
//                        f.isAnnotationPresent(
//                                Data::class.java
//                        )
//                    }
//                    .peek { f: Field -> f.isAccessible = true }
//                    .map { f: Field? -> AnnotatedDataField(bloodstream, f!!, group) }
//                    .collect(Collectors.toList())
//        }
//
//        @Contract(pure = true)
//        fun getOnCalls(bloodstream: Any): Map<Any, AnnotatedPublisher> {
//            val methods = ArrayList<Method>()
//            var searchTargetClass: Class<*>? = bloodstream.javaClass
//            val group = bloodstream.javaClass.getAnnotation(Pack::class.java)?.group
//                    ?: bloodstream.javaClass.simpleName
//            while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//                methods.addAll(listOf(*searchTargetClass.declaredMethods))
//                searchTargetClass = searchTargetClass.superclass
//            }
//            return methods.stream()
//                    .filter { m: Method ->
//                        m.isAnnotationPresent(
//                                Data::class.java
//                        )
//                    }
//                    .peek { m: Method -> m.isAccessible = true }
//                    .map { m: Method -> AnnotatedPublisher(bloodstream, m, group) }
//                    .collect(
//                            Collectors.toMap(
//                                    AnnotatedTarget<*, *>::parentInstance
//                            ) { toCall: AnnotatedPublisher -> toCall }
//                    )
//        }
//    }
	fun mapSettings(function: SettingsMapper.() -> Unit) {
		val mapper = SettingsMapper()
		mapper.function()
		mapper.settingsMap.forEach {
			settingsMap[it.key] = it.value.build()
		}
	}

	class SettingsMapper {
		internal val settingsMap = HashMap<String, SettingsBuilder>()
		fun setFor(group: String, apply: SettingsBuilder.() -> Unit) {
			settingsMap.putIfAbsent(group, SettingsBuilder());
			settingsMap[group]?.apply()
		}

		class SettingsBuilder() {
			private val renders = arrayListOf<RenderOrder.Render<*>>()

			fun <T> with(render: RenderOrder.Render<T>, apply: T.() -> Unit) {
				render.settings.apply()
				renders.add(render)
			}

			internal fun build(): RenderOrder {
				return RenderOrder(renders)
			}
		}
	}

	fun publishMessage(group: String, label: String, contents: String) {
		rendererHashMap.putIfAbsent(group, CartonComponentRenderer.Builder(settingsMap[group]
				?: defaultRenderOrder).add(MessageBoard::class.java).build(group))
		val renderer = rendererHashMap[group] ?: return
		renderer.add(MessageBoard::class.java, TimedDataLine(startTime, label, contents))
	}

	fun publishMessage(group: String, contents: String) {
		publishMessage(group, "", contents)
	}
}
