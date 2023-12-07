package dev.frozenmilk.util.cell

/**
 * a nullable version of [RefCell], that makes an assertion that a value must not be null before it is retrieved, but its contents could be null
 *
 * @see [LateInitCell]
 */
open class UnsafeCell<T>(ref: T? = null, private val error: String = "Attempted to obtain a null value from a safe cell") : RefCell<T?>(ref) {
	override fun get(): T {
		return super.get() ?: throw IllegalStateException(error)
	}
}

/**
 * non-mutating
 *
 * converts a cell into an unsafe cell
 */
fun <T> Cell<T>.intoUnsafeCell(): UnsafeCell<T> = UnsafeCell(this.get())

/**
 * non-mutating
 *
 * @return a [RefCell] that can't take or give null
 */
fun <T> Cell<T?>.intoSafeCell(): Cell<T> = RefCell(get()!!)