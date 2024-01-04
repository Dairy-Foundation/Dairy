package dev.frozenmilk.util.cell

open class RefCell<T> (protected var internalCell: Cell<T>) : CellBase<T>() {
	constructor(ref: T) : this(InnerCell(ref))
	override fun get(): T {
		lastGet = System.nanoTime()
		return internalCell.get()
	}
	override fun accept(p0: T) {
		lastSet = System.nanoTime()
		internalCell.accept(p0)
	}
}

@JvmName("CellUtils")
fun <T> T.intoCell() = RefCell(this)