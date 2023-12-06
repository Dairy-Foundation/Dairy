package dev.frozenmilk.dairy.core.cell

/**
 * a nullable version of [RefCell], that makes an assertion that a value must not be null before it is retrieved
 */
open class SafeCell<T>(ref: T? = null, private val error: String = "Attempted to obtain a null value from a safe cell") : RefCell<T?>(ref) {
	override fun get(): T {
		return super.get() ?: throw IllegalStateException(error)
	}
}