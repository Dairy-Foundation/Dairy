package datacarton.processors

import CSVLog
import datacarton.CartonComponentRenderer
import datacarton.DataBlock

class LogPublicationProcessor(val shelfLife: Int = 7, val recordTime: Boolean = true) : PublicationProcessor {
	val logMap = mutableMapOf<String, CSVLog>()
	override fun initPublication() {
	}

	override fun updatePublication() {
		TODO("Not yet implemented")
	}

	override fun ignoreUpdate(): Boolean = false

	override fun accept(p0: CartonComponentRenderer) {
		if (!logMap.contains(p0.title)) {
			val headings = p0.cartonComponents
					.filterIsInstance<DataBlock>()
					.map { it.dataLines }
					.map { dataLines -> dataLines.mapNotNull { it?.label } }
			
			logMap[p0.title] = CSVLog(p0.title, shelfLife, recordTime)
					.setHeadings()
		}
	}
}