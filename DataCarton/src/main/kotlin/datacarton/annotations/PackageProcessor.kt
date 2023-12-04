package datacarton.annotations

import collections.annotatedtargets.AnnotatedData
import collections.annotatedtargets.Flatpackaged
import datacarton.CartonComponent
import datacarton.DataBlock
import datacarton.DataLine
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

interface PackageProcessor {
	fun process(instance: Supplier<*>, group: String, flatpack: Flatpackaged?): Stream<ProcessedOutput>;
	data class ProcessedOutput(val group: String, val cartonComponentClass: Class<out CartonComponent>, val dataLine: DataLine?)
}

/**
 * processes @Data annotations
 */
object DataPackageProcessor : PackageProcessor {
	override fun process(instance: Supplier<*>,
						 group: String,
						 flatpack: Flatpackaged?): Stream<PackageProcessor.ProcessedOutput> {
		val outputs = ArrayList<PackageProcessor.ProcessedOutput>()
		var searchTargetClass: Class<*>? = instance.get().javaClass

		while (searchTargetClass != null && searchTargetClass != Any::class.java) {
			outputs += listOf(*searchTargetClass.declaredMethods, *searchTargetClass.declaredFields).stream()
					.filter { it.isAnnotationPresent(Data::class.java) }
					.filter { flatpack == null || (it.isAnnotationPresent(Flatten::class.java) && flatpack.flatpack.includeDefaults) }
					.peek { it.isAccessible = true }
					.map {
						AnnotatedData(instance, it, group, flatpack != null)
					}
					.map {
						PackageProcessor.ProcessedOutput(it.group, DataBlock::class.java, DataLine(it.label) { it.childInstance.get().toString() })
					}
					.collect(Collectors.toList())

			searchTargetClass = searchTargetClass.superclass
		}
		return outputs.stream()
	}
}

/**
 * processes @Publishes annotations
 */
//class MessagePackageProcessor : PackageProcessor {
//    override fun process(instance: Supplier<*>, group: String, flatpack: Flatpackaged?): Stream<PackageProcessor.ProcessedOutput> {
//        val outputs = ArrayList<PackageProcessor.ProcessedOutput>()
//        var searchTargetClass: Class<*>? = instance.get().javaClass
//
//        while (searchTargetClass != null && searchTargetClass != Any::class.java) {
//            outputs += searchTargetClass.declaredMethods.toList().stream()
//                    .filter { it.isAnnotationPresent(Publishes::class.java) }
//                    .map {
//                        PackageProcessor.ProcessedOutput(flatpack?.group
//                                ?: group, MessageBoard::class.java, null)
//                    }
//                    .collect(Collectors.toList())
//
//            searchTargetClass = searchTargetClass.superclass
//        }
//        return outputs.stream()
//    }
//}