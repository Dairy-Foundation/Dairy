package datacarton

import collections.annotatedtargets.*
import datacarton.annotations.Flatpack
import datacarton.annotations.Pack
import datacarton.annotations.PackageProcessor
import datacarton.annotations.PublicationProcessor
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.util.*
import java.util.function.Supplier
import kotlin.collections.Collection
import kotlin.collections.HashMap

@Suppress("unused")
class DataCarton private constructor(private val defaultRenderOrder: RenderOrder, private val publicationProcessors: Collection<PublicationProcessor<*>>, private val packageProcessors: Collection<PackageProcessor>) {

	//    private val output: Field = outputField
	private val rendererHashMap: HashMap<String, CartonComponentRenderer> = HashMap()

	//    private lateinit var instanceGroups: WeakHashMap<Any, Pair<Boolean, String>>
	private val startTime: Long = System.nanoTime()
	private val settingsMap = HashMap<String, RenderOrder>()

//	constructor(telemetry: Telemetry, renderOrder: RenderOrder) {
//		telemetry.isAutoClear = false
//		telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE)
//		telemetry.captionValueSeparator = ""
//		telemetry.itemSeparator = ""
//		try {
//			outputHolder = telemetry.addLine("")
//			output = outputHolder.javaClass.getField("lineCaption")
//			output.isAccessible = true
//		} catch (e: NoSuchFieldException) {
//			throw RuntimeException(e)
//		}
//		traceComponents = HashMap()
//		this.renderOrder = renderOrder
//		startTime = System.nanoTime()
//		onCalls = LinkedHashMap()
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
	 * recursively finds the fields which contain traces in the blood stream
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

	class Builder {
		private var defaultRenderOrder: RenderOrder? = null;
		private val publicationProcessors = emptySet<PublicationProcessor<*>>().toMutableSet()
		private val packageProcessors = emptySet<PackageProcessor>().toMutableSet()

		fun renderWithByDefault(defaultRenderOrder: RenderOrder): Builder {
			this.defaultRenderOrder = defaultRenderOrder
			return this
		}

		fun addPublicationProcessors(vararg publicationProcessors: PublicationProcessor<*>): Builder {
			this.publicationProcessors.addAll(publicationProcessors)
			return this
		}

		fun addPackageProcessors(vararg packageProcessors: PackageProcessor): Builder {
			this.packageProcessors.addAll(packageProcessors)
			return this
		}

		fun buildIntoInstance(): DataCarton {
			if (defaultRenderOrder == null) throw RuntimeException("RenderOrder left unconfigured")
			if (publicationProcessors.isEmpty()) throw RuntimeException("PublicationProcessors empty")
			if (packageProcessors.isEmpty()) throw RuntimeException("PackageProcessors empty")
			instance = DataCarton(defaultRenderOrder!!, publicationProcessors, packageProcessors)
			return instance!!
		}
	}

	companion object {
		private var instance: DataCarton? = null

		/**
		 * sets the instance to be null
		 */
		fun drop() {
			instance = null
		}

		fun publishMessage(group: String, label: String, contents: String) {
			if (instance == null) return;
			instance!!.rendererHashMap.putIfAbsent(group, CartonComponentRenderer.Builder(instance!!.settingsMap[group]
					?: instance!!.defaultRenderOrder).add(MessageBoard::class.java).build(group))
			val renderer = instance!!.rendererHashMap[group] ?: return
			renderer.add(MessageBoard::class.java, TimedDataLine(instance!!.startTime, label, contents))
		}

		fun publishMessage(group: String, contents: String) {
			publishMessage(group, "", contents)
		}

//        fun publishMessage(instance: Any, flatten: Boolean, label: String, contents: String) {
//            val pair = this.instance?.instanceGroups?.get(instance) ?: return
//            if ((pair.first && !flatten)) return
//            val group = pair.second
//            publishMessage(group, label, contents)
//        }
//
//        fun publishMessage(instance: Any, flatten: Boolean, contents: String) {
//            publishMessage(instance, flatten, "", contents)
//        }
//
//        fun publishMessage(instance: Any, label: String, contents: String) {
//            publishMessage(instance, false, label, contents)
//        }
//
//        fun publishMessage(instance: Any, contents: String) {
//            publishMessage(instance, false, contents)
//        }
	}
}
