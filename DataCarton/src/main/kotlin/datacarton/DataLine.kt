package datacarton

import java.util.function.Supplier

open class DataLine internal constructor(
		/**
		 * represents the origin of the message
		 */
		val label: String,
		/**
		 * contents of the message
		 */
		val contents: Supplier<String>,

		) {
	val labelWidth: Int
		get() = label.length

	fun groupToOutput(labelWidth: Int): String {
		val builder = StringBuilder(label)
		for (len in label.length until labelWidth) {
			builder.append(" ")
		}
		builder.append(" | ")
		builder.append(contents.get())
		return builder.toString()
	}

	override fun toString(): String {
		return if (label != "") {
			label + ": " + contents.get()
		} else contents.get()
	}
}
