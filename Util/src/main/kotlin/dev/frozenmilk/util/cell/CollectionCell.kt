package dev.frozenmilk.util.cell

/**
 * A [RefCell] that also acts as a collection
 */
class CollectionCell<T>(collection: Cell<Collection<T>>) : RefCell<Collection<T>>(collection), Collection<T> by collection.get() {
	constructor(collection: Collection<T>) : this(InnerCell(collection))
}

@JvmName("CellUtils")
fun <T> Collection<T>.intoCollectionCell() = CollectionCell(this)