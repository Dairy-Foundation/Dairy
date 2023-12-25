package log

class CSVLog @JvmOverloads constructor(directoryName: String, parents: Collection<String> = emptyList(), shelfLife: Int = 7, val recordTime: Boolean = true) : Log(directoryName, parents, "csv", shelfLife) {
    private val data = mutableMapOf<String, Any?>()
    private var order = emptyList<String>()
    private var inited = false
    private var startTime = 0L
    fun setHeadings(vararg headings: String): CSVLog {
        check(!inited) { "headings cannot be set while the file is being written to" }
        order = if (recordTime) listOf("time") + headings
        else headings.toList()
        return this
    }

    fun logData(heading: String, data: Any?): CSVLog {
        this.data[heading] = data
        return this
    }

    private fun formatString(string: String): String {
        if (string.any { it in charArrayOf(',', '\n', '\r', '\"', '"') }) return "\"$string\""
        return string
    }

    fun update(): CSVLog {
        if (!inited) {
            order.forEach { heading ->
                val out = "${formatString(heading)},"
                out.forEach { writer.append(it) }
            }
            startTime = System.nanoTime()
            inited = true
        }
        if (recordTime) data["time"] = (System.nanoTime() - startTime) / 1E9
        order.forEach { heading ->
            val out = "${formatString(data[heading]?.toString() ?: "")},"
            out.forEach { writer.append(it) }
        }
        writer.append("\n")
        data.clear()
        return this
    }
}