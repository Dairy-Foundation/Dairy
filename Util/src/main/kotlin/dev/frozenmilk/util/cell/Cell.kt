package dev.frozenmilk.util.cell

import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.max
import kotlin.reflect.KProperty

/**
 * a top level cell interface, cells act like pointers, and recreate many features of them
 */
interface Cell<T> : Consumer<T>, Supplier<T> {
	operator fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = accept(value)
}

abstract class CellBase<T> : Cell<T> {
	protected var lastGet = System.nanoTime()
	/**
	 * in seconds
	 */
	val timeSincelastGet : Double
		get() = (System.nanoTime() - lastGet) / 1E9

	protected var lastSet = System.nanoTime()
	/**
	 * in seconds
	 */
	val timeSincelastSet : Double
		get() = (System.nanoTime() - lastSet) / 1E9

	/**
	 * in seconds
	 */
	val timeSinceLastAccess: Double
		get() = max(timeSincelastGet, timeSincelastSet)

	override fun toString(): String = get().toString()
}

internal class InnerCell<T>(private var ref: T) : Cell<T> {
	override fun accept(p0: T) { ref = p0 }

	override fun get(): T = ref
}
