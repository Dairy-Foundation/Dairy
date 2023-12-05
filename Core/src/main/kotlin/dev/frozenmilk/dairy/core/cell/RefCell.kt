package dev.frozenmilk.dairy.core.cell

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * a transparent cell
 *
 * @see [SafeCell]
 * @see [dev.frozenmilk.dairy.core.cell.reinitialisable.MirroredRefCell]
 */
open class RefCell<T>(private var ref: T) : Supplier<T>, Consumer<T> {
	override fun get(): T {
		return ref
	}

	override fun accept(p0: T) {
		this.ref = p0;
	}

	override fun toString(): String = get().toString()

	override fun hashCode(): Int {
		return get().hashCode()
	}

	override fun equals(other: Any?): Boolean {
		return get()?.equals(other) ?: false
	}
}