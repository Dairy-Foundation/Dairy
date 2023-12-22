package datacarton.processors

import collections.annotatedtargets.ImportingPackaged
import datacarton.CartonComponent
import datacarton.DataLine
import java.util.function.Supplier
import java.util.stream.Stream

interface PackageProcessor {
	fun process(instance: Supplier<*>, group: String, importer: ImportingPackaged?): Stream<ProcessedOutput>;
	data class ProcessedOutput(val group: String, val cartonComponentClass: Class<out CartonComponent>, val dataLine: DataLine?)
}
