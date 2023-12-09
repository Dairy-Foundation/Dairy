package dev.frozenmilk.util.cell

import kotlin.reflect.KProperty

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

	override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = accept(value)

	override fun equals(other: Any?): Boolean {
		return get()?.equals(other) ?: false
	}
}
fun <T> cellOf(ref: T) = RefCell(ref)

/**
 * non-mutating
 */
fun <T> Cell<T>.toRefCell(): RefCell<T> = RefCell(this.get())
