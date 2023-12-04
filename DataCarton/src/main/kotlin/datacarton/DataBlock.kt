package datacarton

import collections.DataLineArrayList
import datacarton.CartonComponent.TraceComponentBuilder

class DataBlock(private val dataLines: DataLineArrayList<DataLine?>) : CartonComponent {
	override fun toString(): String {
		val builder = StringBuilder()
		for (line in dataLines) {
			if (line != null) {
				builder.append(line.groupToOutput(dataLines.labelWidth)).append("\n")
			}
		}
		return builder.toString()
	}

	override fun getData(): Collection<DataLine?> {
		return dataLines
	}

	override fun add(dataLine: DataLine?) {
		dataLines.add(dataLine)
	}

	class Builder : TraceComponentBuilder {
		private val dataLines = DataLineArrayList<DataLine?>()
		override fun add(dataLine: DataLine?) {
			dataLines.add(dataLine)
		}

		override fun build(settings: Any?): CartonComponent? {
			return if (dataLines.isEmpty()) null else DataBlock(dataLines)
		}
	}

	class Settings
}
