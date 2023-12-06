package dev.frozenmilk.dairy.core.collections.cell

import java.util.function.Supplier

/**
 * lazily loads a value when contents are null
 */
class LazyCell<T>(private val supplier: Supplier<T>) : LateInitCell<T>("Attempted to obtain a null value from a LazyCell") {
	private var inited = false
	override fun get(): T {
		if(!inited) accept(supplier.get())
		inited = true
		return super.get()
	}

	override fun accept(p0: T) {
		if (p0 == null) inited = false
		super.accept(p0)
	}
}