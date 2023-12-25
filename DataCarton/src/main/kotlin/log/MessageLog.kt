package log

class MessageLog(directoryName: String, parents: Collection<String> = emptyList(), shelfLife: Int = 7) : Log(directoryName, parents, "txt", shelfLife) {
	fun publish(message: Any?) {
		message.toString().forEach {
			writer.append(it)
		}
		writer.append("\n")
	}
}