package dev.frozenmilk.util.cell

import kotlin.math.max

open class LateInitCell<T> (protected var internalCell: Cell<T?>, protected val error: String = "Attempted to obtain a null value from an unsafe Cell") : CellBase<T>() {
	@JvmOverloads
	constructor(ref: T? = null, error: String = "Attempted to obtain a null value from an unsafe Cell") : this(InnerCell(ref), error)
	override fun get(): T  {
		lastGet = System.nanoTime()
		return internalCell.get() ?: throw IllegalStateException(error)
	}

	/**
	 * returns null instead of throwing an error
	 */
	fun safeGet(): T? {
		lastGet = System.nanoTime()
		return internalCell.get()
	}

	override fun accept(p0: T) {
		lastSet = System.nanoTime()
		internalCell.accept(p0)
	}

	/**
	 * causes the next attempt to get the contents of the cell to fail
	 */
	fun invalidate() = internalCell.accept(null)

	fun initialised(): Boolean = safeGet() != null

	/**
	 * applies the function and returns the result if the internals are already initialised, else return null
	 *
	 * DOES NOT evaluate the contents of the cell, if they are in an [invalidate]d state
	 */
	fun <R> safeInvoke(fn: (T) -> R): R? = safeGet()?.let(fn)

	override fun toString(): String = safeGet().toString()
}

@JvmName("CellUtils")
fun <T> T.intoLateInitCell() = LateInitCell(this)
