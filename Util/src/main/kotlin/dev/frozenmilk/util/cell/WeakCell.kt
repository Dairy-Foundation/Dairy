package dev.frozenmilk.util.cell

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * a cell that contains a weak reference, which gets automatically dropped by the
 */
class WeakCell<T>(ref: T) : Cell<T> {
	private var weakRef = WeakReference(ref)
	override fun get(): T {
		return weakRef.get()!!
	}

	override fun accept(p0: T) {
		this.weakRef = WeakReference(p0);
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

/**
 * non-mutating
 */
fun <T> Cell<T>.intoWeakCell(): WeakCell<T> = WeakCell(this.get())
