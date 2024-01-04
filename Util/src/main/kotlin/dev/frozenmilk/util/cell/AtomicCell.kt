package dev.frozenmilk.util.cell

import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class AtomicCell<T>(ref: T) : Cell<T>, AtomicReference<T>(ref) {
	override fun accept(p0: T) {
		set(p0)
	}

	private var lastGet = System.nanoTime()
	/**
	 * in seconds
	 */
	val timeSincelastGet : Double
		get() = (System.nanoTime() - lastGet) / 1E9

	private var lastSet = System.nanoTime()
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

@JvmName("CellUtils")
fun <T> T?.intoAtomicCell() = AtomicCell(this)