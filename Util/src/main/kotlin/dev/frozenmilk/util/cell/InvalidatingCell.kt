package dev.frozenmilk.util.cell

import java.util.function.Supplier

open class InvalidatingCell<T>(supplier: Supplier<T>, var invalidator: Supplier<Boolean>) : LazyCell<T>(supplier) {
	override fun get(): T {
		if (invalidator.get()) invalidate()
		return super.get()
	}
}

@JvmName("CellUtils")
fun <T> Supplier<T>.intoInvalidatingCell(invalidator: Supplier<Boolean>) = InvalidatingCell(this, invalidator)