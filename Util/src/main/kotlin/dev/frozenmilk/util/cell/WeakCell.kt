package dev.frozenmilk.util.cell

import java.lang.ref.WeakReference
import java.util.function.Supplier

private class InternalWeakCell<T>(ref: T) : Cell<T?> {
	private var weakRef = WeakReference(ref)
	override fun get(): T? {
		return weakRef.get()
	}
	override fun accept(p0: T?) {
		this.weakRef = WeakReference(p0);
	}
}

/**
 * a cell that contains a weak reference, which gets automatically dropped by the garbage collector
 *
 * @see LazyWeakCell
 */
class WeakCell<T> private constructor(internalWeakCell: InternalWeakCell<T>) : LateInitCell<T>(internalWeakCell, "attempted to access the dropped contents of a WeakCell") {
	constructor(ref: T) : this(InternalWeakCell(ref))
}

@JvmName("CellUtils")
fun <T> T.intoWeakCell() = WeakCell(this)

///**
// * a cell that contains a weak reference, which gets automatically dropped by the garbage collector, acts like a [LazyCell]
// *
// * @see WeakCell
// */
//class LazyWeakCell<T>(private val supplier: Supplier<T>) : LateInitCell<T>(WeakCell<null>, "attempted to access the dropped contents of a WeakCell") {
//	/**
//	 * the time, in seconds, since last time the contents of this cell started being lazily evaluated, due to an [invalidate]d state
//	 */
//	val timeSinceBeforeLastEval: Double
//		get() {
//			return (System.nanoTime() - timeBeforeLastEval) / 1E9
//		}
//
//	/**
//	 * the time, in seconds, since last time the contents of this cell finished being lazily evaluated, due to an [invalidate]d state
//	 */
//	val timeSinceAfterLastEval: Double
//		get() {
//			return (System.nanoTime() - timeAfterLastEval) / 1E9
//		}
//
//	private var timeBeforeLastEval = 0L
//	private var timeAfterLastEval = 0L
//
//	override fun get(): T {
//		if(!initialised()) {
//			timeBeforeLastEval = System.nanoTime()
//			accept(supplier.get())
//			timeAfterLastEval = System.nanoTime()
//		}
//		return super.get()
//	}
//}
//
//@JvmName("CellUtils")
//fun <T> Supplier<T>.intoWeakLazyCell() = LazyWeakCell(this)