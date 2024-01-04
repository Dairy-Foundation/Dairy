package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * @param timeOut time in seconds after which the contents of the cell are no-longer valid, and so become stale and invalid, this timer gets reset after the contents get evaluated
 */
class StaleEvalCell<T>(var timeOut: Double, supplier: Supplier<T>) : InvalidatingCell<T>(supplier, Supplier { false }) {
	init {
		invalidator = Supplier {
			this.timeSinceAfterLastEval > timeOut
		}
	}
}

@JvmName("CellUtils")
fun <T> Supplier<T>.intoStaleEvalCell(timeOut: Double) = StaleEvalCell(timeOut, this)