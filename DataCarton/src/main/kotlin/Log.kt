//import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import java.io.File
import java.io.FileWriter
import java.io.IOException

@Suppress("unused")
class Log(system: String, storedLogs: Int, private val enabled: Boolean, vararg dataHeadings: String) {
    private val startTime: Long = System.nanoTime()
    private val now: String = (System.nanoTime() / 1E9).toString()
    private val dataLine: Array<String?>
    private val dataHeadings: LinkedHashMap<String, Int> = LinkedHashMap()
    private var fileWriter: FileWriter? = null

    init {
        this.dataHeadings["ElapsedTime"] = 0
        var i = 1
        for (heading in dataHeadings) {
            this.dataHeadings[heading] = i
            i++
        }
        dataLine = arrayOfNulls(dataHeadings.size + 1)
        if (enabled) {
            val directoryPath = clearToDirectoryPath(system, storedLogs, "") // TODO fix
            TODO("root folder not implemented")
            fileWriter = try {
                FileWriter(directoryPath + "/" + system + "0.csv", true)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            createHeadings()
        }
    }

    constructor(subsystem: String, enabled: Boolean, vararg dataHeadings: String) : this(
            system = subsystem,
            storedLogs = 6,
            enabled = enabled,
            dataHeadings = dataHeadings
    )

    fun updateLoop(storeTime: Boolean) {
        if (!enabled) {
            return
        }
        val elapsedTime = (System.nanoTime() - startTime) / 1E9
        val dataWrite = StringBuilder()
        if (storeTime) {
            dataLine[0] = elapsedTime.toString()
        }
        for (i in dataLine.indices) {
            dataWrite.append(dataLine[i])
            if (i != dataLine.size - 1) {
                dataWrite.append(",")
            }
        }
        try {
            fileWriter!!.write(dataWrite.toString() + "\n")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * @param dataHeading a previously set data heading
     * @param data        the data object to store, Object.toString is automatically called on it
     */
    fun logData(dataHeading: String, data: Any) {
        if (!enabled) {
            return
        }
        val dataHeadingArrayPosition = dataHeadings[dataHeading] ?: return
        dataLine[dataHeadingArrayPosition] = data.toString()
    }

    fun close() {
        if (!enabled) {
            return
        }
        try {
            fileWriter!!.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun createHeadings() {
        if (!enabled) {
            return
        }
        try {
            fileWriter!!.write(now + "\n")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        for ((i, heading) in dataHeadings.keys.withIndex()) {
            dataLine[i] = heading
        }
        updateLoop(false)
    }

    companion object {
        /**
         * removes oldest log and shifts all the others down one
         *
         * @param system     name of the logFile
         * @param storedLogs number of logs to be stored
         * @return the path to the log folder
         */
        private fun clearToDirectoryPath(system: String, storedLogs: Int, rootFolder: String): String {
//            val directory: File = File(AppUtil.FIRST_FOLDER, "/dairy/logs/$system")
            val directory: File = File(rootFolder, "/dairy/logs/$system")
            directory.mkdirs()
            val outdatedLog = File(directory, system + (storedLogs - 1) + ".csv")
            outdatedLog.delete()
            for (i in storedLogs downTo 1) {
                val oldLog = File(directory, system + (i - 1) + ".csv")
                val oldLogDestination = File(directory, "$system$i.csv")
                oldLog.renameTo(oldLogDestination)
            }
            return directory.path
        }
    }
}
