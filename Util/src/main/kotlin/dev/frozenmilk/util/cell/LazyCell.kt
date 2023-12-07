package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * lazily loads a value when contents are null
 */
open class LazyCell<T>(private val supplier: Supplier<T>) : LateInitCell<T>("Attempted to obtain a null value from a LazyCell"), Lazy<T> {
	private var inited = false
	final override fun get(): T {
		if(!inited) accept(supplier.get())
		inited = true
		return super.get()
	}

	final override fun accept(p0: T) {
		if (p0 == null) inited = false
		super.accept(p0)
	}

	final override val value: T = get()

	final override fun isInitialized(): Boolean = inited
}
