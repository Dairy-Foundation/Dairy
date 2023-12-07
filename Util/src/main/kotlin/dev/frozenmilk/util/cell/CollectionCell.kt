package dev.frozenmilk.util.cell

/**
 * A [RefCell] that also acts as a collection
 */
class CollectionCell<T>(collection: Collection<T>) : RefCell<Collection<T>>(collection), Collection<T> by collection