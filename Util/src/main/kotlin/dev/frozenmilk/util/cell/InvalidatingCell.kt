package dev.frozenmilk.util.cell

import java.util.function.Supplier

open class InvalidatingCell<T>(supplier: Supplier<T>, var invalidator: Supplier<Boolean> = Supplier { false }) : LazyCell<T>(supplier) {
	override fun get(): T {
		if (invalidator.get()) invalidate()
		return super.get()
	}
}