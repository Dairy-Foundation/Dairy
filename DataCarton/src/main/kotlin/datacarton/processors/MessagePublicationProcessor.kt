package datacarton.processors

import CSVLog
import datacarton.CartonComponentRenderer
import datacarton.DataBlock

class DataLogPublicationProcessor(val directory: String, val shelfLife: Int = 7, val recordTime: Boolean = true) : PublicationProcessor {
	private val logMap = mutableMapOf<String, CSVLog>()
	override fun initPublication() {
	}

	override fun updatePublication() {
		logMap.forEach { (_, log) -> log.update() }
	}

	override fun ignoreUpdate(): Boolean = false

	override fun accept(p0: CartonComponentRenderer) {
		val dataMap = p0.cartonComponents
				.filterIsInstance<DataBlock>()
				.flatMap { it.dataLines }
				.mapNotNull {
					val label = it?.label ?: return@mapNotNull null
					val contents = it.contents.get()
					return@mapNotNull label to contents
				}
		val headings = dataMap.map { it.first }.toTypedArray()

		if (!logMap.contains(p0.title)) {
			logMap[p0.title] = CSVLog("${p0.title}_data", listOf(directory), shelfLife, recordTime)
					.setHeadings(*headings)
		}

		val log = logMap[p0.title]!!

		dataMap.forEach {
			log.logData(it.first, it.second)
		}
	}
}