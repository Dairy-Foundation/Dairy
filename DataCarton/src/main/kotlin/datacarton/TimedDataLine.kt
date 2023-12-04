package datacarton

import java.util.function.Supplier
import kotlin.math.roundToInt

class TimedDataLine internal constructor(startTime: Long, label: String, contents: Supplier<String>) :
        DataLine(
                "[" + ((System.nanoTime() - startTime) * 100.0 / 1E9).roundToInt() / 100.0 + "]" + if (label != "") " $label" else "", contents
        ) {
    constructor(startTime: Long, label: String, contents: String) : this(
            startTime,
            label,
            Supplier<String> { contents })
}
