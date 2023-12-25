package datacarton.processors

import datacarton.CartonComponentRenderer
import datacarton.MessageBoard
import log.MessageLog

class MessageLogPublicationProcessor(val directory: String, val shelfLife: Int = 7) : PublicationProcessor {
	private val logMap = mutableMapOf<String, MessageLog>()
	private val prevMap = mutableMapOf<String, String>()
	override fun initPublication() {
	}

	override fun updatePublication() {
	}

	override fun ignoreUpdate(): Boolean = false

	override fun accept(p0: CartonComponentRenderer) {
		val dataMap = p0.cartonComponents
				.filterIsInstance<MessageBoard>()
				.map { it.getData() }
				.mapNotNull { it.first() }

		if (!logMap.contains(p0.title)) {
			logMap[p0.title] = MessageLog("${p0.title}_messages", listOf(directory), shelfLife)
		}

		dataMap.forEach {
			if (prevMap[p0.title] != it.toString()) {
				logMap[p0.title]!!.publish(it)
				prevMap[p0.title] = it.toString()
			}
		}
	}
}