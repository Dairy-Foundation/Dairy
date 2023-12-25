package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * lazily loads a value when contents are null
 */
open class LazyCell<T>(private val supplier: Supplier<T>) : LateInitCell<T>("Attempted to obtain a null value from a LazyCell") {
	private var initialised = false

	/**
	 * the time, in seconds, since last time the contents of this cell started being lazily evaluated, due to an [invalidate]d state
	 */
	val timeSinceBeforeLastEval: Double
		get() {
			return (System.nanoTime() - timeBeforeLastEval) / 1E9
		}

	/**
	 * the time, in seconds, since last time the contents of this cell finished being lazily evaluated, due to an [invalidate]d state
	 */
	val timeSinceAfterLastEval: Double
		get() {
			return (System.nanoTime() - timeAfterLastEval) / 1E9
		}

	private var timeBeforeLastEval = 0L
	private var timeAfterLastEval = 0L
	override fun get(): T {
		if(!initialised) {
			timeBeforeLastEval = System.nanoTime()
			accept(supplier.get())
			timeAfterLastEval = System.nanoTime()
		}
		initialised = true
		return super.get()
	}

	override fun accept(p0: T) {
		if (p0 == null) initialised = false
		super.accept(p0)
	}

	/**
	 * causes the next attempt to get the contents of the cell to recalculate from the [supplier]
	 */
	fun invalidate() {
		initialised = false
	}

	/**
	 * applies the function and returns the result if the internals are already initialised, else return null
	 *
	 * DOES NOT evaluate the contents of the cell, if they are in an [invalidate]d state
	 */
	fun <R> safeApply(apply: (T) -> R): R? {
		if(initialised) return apply(get())
		return null
	}
}

/**
 * causes the next attempt to get the contents of the cell to recalculate from the [LazyCell.supplier]
 *
 * also sets the contents to null
 */
fun <T> LazyCell<T?>.cleanse() {
	accept(null)
}
