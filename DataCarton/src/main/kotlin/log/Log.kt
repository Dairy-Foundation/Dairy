package log

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
abstract class Log(directoryName: String, parents: Collection<String>, fileExtension: String, shelfLife: Int) {
    val writer: BufferedWriter
    init {
        val directory = File(AppUtil.FIRST_FOLDER, "/DataCarton/logs/${
            parents.reduce { acc, s -> 
                check(s.isNotEmpty()) { "directory cannot be empty" }
                "$acc/$s" 
            }
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
