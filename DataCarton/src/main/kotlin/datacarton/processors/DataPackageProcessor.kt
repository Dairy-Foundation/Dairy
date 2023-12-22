package datacarton.processors

import collections.annotatedtargets.AnnotatedData
import collections.annotatedtargets.ImportingPackaged
import datacarton.DataBlock
import datacarton.DataLine
import datacarton.annotations.Data
import datacarton.annotations.Export
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * processes @Data annotations
 */
object DataPackageProcessor : PackageProcessor {
	override fun process(instance: Supplier<*>,
						 group: String,
						 importer: ImportingPackaged?): Stream<PackageProcessor.ProcessedOutput> {
		val outputs = ArrayList<PackageProcessor.ProcessedOutput>()
		var searchTargetClass: Class<*>? = instance.get()::class.java

		while (searchTargetClass != null && searchTargetClass != Any::class.java) {
			outputs += listOf(*searchTargetClass.declaredMethods, *searchTargetClass.declaredFields).stream()
					.filter { it.isAnnotationPresent(Data::class.java) || importer?.dataFields?.contains(it) == true || importer?.dataMethods?.contains(it) == true}
					.filter {
						if (importer == null) return@filter true

						var bundled = importer.includeDefaults
						bundled = bundled && it.isAnnotationPresent(Export::class.java)
						if (bundled) {
							val exporter = it.getAnnotation(Export::class.java)!!
							return@filter exporter.bundle
						}

						return@filter (it in importer.dataFields || it in importer.dataMethods)
					}
					.peek { it.isAccessible = true }
					.map {
						var bundled = importer?.includeDefaults ?: false
						bundled = bundled && it.isAnnotationPresent(Export::class.java)
						if (bundled) {
							val exporter = it.getAnnotation(Export::class.java)!!
							bundled = exporter.bundle
						}

						AnnotatedData(instance, it, group, bundled)
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