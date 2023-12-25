import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * sets up files in the FIRST/DataCarton/logs/ directory, trims old files, and generates a new file with the current date as a name
 *
 * @param fileExtension should not include the '.' character
 * @param shelfLife number of days that log files should be kept for, logs this many days old will be removed
 */
abstract class LogAssistant(directoryName: String, children: Collection<String>, fileExtension: String, shelfLife: Int) {
    val writer: BufferedWriter
    init {
        val directory = File(AppUtil.FIRST_FOLDER, "/DataCarton/logs/${
            children.reduce { acc, s -> "$acc/$s" }
        }/$directoryName")
        // clean dir
        val dateFormat = SimpleDateFormat("hh:mm_dd-MM-yyyy", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        val expirationDate = Date(System.currentTimeMillis() - (86400 * shelfLife))
        directory.mkdirs()
        directory.listFiles()?.forEach {
            if (!it.isFile) {
                it.deleteRecursively()
                return@forEach
            }
            val parsed = try {
                dateFormat.parse(it.name)
            } catch (e: ParseException) {
                it.delete()
                return@forEach
            }
            if (parsed.before(expirationDate)) {
                it.delete()
                return@forEach
            }
            if (parsed == date) {
                it.delete()
                return@forEach
            }
        }
        // set up new file
        val file = File(directory, dateFormat.format(date) + ".$fileExtension")
        writer = BufferedWriter(FileWriter(file))
    }

    fun close() {
        writer.flush()
        writer.close()
    }

    protected fun finalise() {
        close()
    }
}

class CSVLog @JvmOverloads constructor(directoryName: String, children: Collection<String> = emptyList(), shelfLife: Int = 7, val recordTime: Boolean = true) : LogAssistant(directoryName, children, "csv", shelfLife) {
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
        if (string.any { it in charArrayOf(',', '\n', '\r', '"') }) return "\"$string\""
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
