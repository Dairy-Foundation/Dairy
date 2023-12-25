package dev.frozenmilk.util.cell

import java.util.function.Supplier

class InvalidatingCell<T>(supplier: Supplier<T>, private val invalidator: Supplier<Boolean>) : LazyCell<T>(supplier) {
	override fun get(): T {
		if (invalidator.get()) invalidate()
		return super.get()
	}
}