package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * lazily loads a value when contents are null
 */
open class LazyCell<T>(private val supplier: Supplier<T>) : LateInitCell<T>("Attempted to obtain a null value from a LazyCell") {
	private var initialised = false
	override fun get(): T {
		if(!initialised) accept(supplier.get())
		initialised = true
		return super.get()
	}

	override fun accept(p0: T) {
		if (p0 == null) initialised = false
		super.accept(p0)
	}

	/**
	 * causes the next attempt to get the contents of the cell to recalculate from the [supplier]
	 */
	fun invalidate() {
		initialised = false
	}
}

/**
 * causes the next attempt to get the contents of the cell to recalculate from the [LazyCell.supplier]
 *
 * also sets the contents to null
 */
fun <T> LazyCell<T?>.cleanse() {
	accept(null)
}
