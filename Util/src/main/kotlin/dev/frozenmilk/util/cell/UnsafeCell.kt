package dev.frozenmilk.util.cell

import kotlin.reflect.KProperty

/**
 * a nullable version of [RefCell], that makes an assertion that a value must not be null before it is retrieved, but its contents could be null
 *
 * @see [LateInitCell]
 */
open class UnsafeCell<T>(private var ref: T? = null, private val error: String = "Attempted to obtain a null value from a safe cell") : Cell<T?> {
	override fun accept(p0: T?) {
		ref = p0
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = accept(value)
	override fun getValue(thisRef: Any?, property: KProperty<*>): T? = get()

	override fun get(): T {
		return ref ?: throw IllegalStateException(error)
	}
}


/**
 * non-mutating
 *
 * converts a cell into an unsafe cell
 */
fun <T> Cell<T>.intoUnsafeCell(): UnsafeCell<T> = UnsafeCell(this.get())

/**
 * non-mutating
 *
 * @return a [RefCell] that can't take or give null
 */
fun <T> Cell<T?>.intoSafeCell(): Cell<T> = RefCell(get()!!)