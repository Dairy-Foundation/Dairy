package dev.frozenmilk.util.cell

import java.util.function.Supplier

/**
 * @param timeOut time in seconds after which the contents of the cell are no-longer valid, and so become stale and invalid, this timer gets reset when the contents are accessed in any manner
 */
class StaleAccessCell<T>(var timeOut: Double, supplier: Supplier<T>) : InvalidatingCell<T>(supplier, Supplier { false }) {
	init {
		invalidator = Supplier {
			this.timeSinceLastAccess > timeOut
		}
	}
}

@JvmName("CellUtils")
fun <T> Supplier<T>.intoStaleAccessCell(timeOut: Double) = StaleEvalCell(timeOut, this)