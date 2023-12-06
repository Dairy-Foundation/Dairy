package dev.frozenmilk.dairy.core.collections.cell

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * a transparent cell
 *
 * @see [Cell]
 */
open class RefCell<T>(private var ref: T) : Cell<T> {
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
fun <T> cellOf(ref: T) = RefCell(ref)

/**
 * non-mutating
 */
fun <T> Cell<T>.toRefCell(): RefCell<T> = RefCell(this.get())
