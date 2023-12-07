package dev.frozenmilk.util.cell

/**
 * a cell that starts with no value, but can be given one, throws an error if an attempt to access its contents are made but [lateInitRef] is null
 *
 * @see [UnsafeCell]
 */
open class LateInitCell<T>(private val error: String = "Attempted to obtain a null value from a LateInitCell") : Cell<T> {
	private var lateInitRef: T? = null
	override fun accept(p0: T) {
		lateInitRef = p0
	}

	override fun get(): T {
		return lateInitRef ?: throw IllegalStateException(error)
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other)
	}

	override fun hashCode(): Int {
		return super.hashCode()
	}

	override fun toString(): String {
		return super.toString()
	}
}
