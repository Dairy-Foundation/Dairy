package dev.frozenmilk.util.cell

/**
 * a cell that acts like a collection that could potentially contain a single element
 */
class SingleCell<T>(ref: T? = null) : UnsafeCell<T>(ref), Collection<T>{
	override val size: Int
		get() = if(get() == null) 0 else 1

	override fun containsAll(elements: Collection<T>): Boolean {
		return get() in elements
	}

	override fun contains(element: T): Boolean {
		return get() == element
	}

	override fun isEmpty() = (get() == null)

	override fun iterator(): Iterator<T> {
		return object : Iterator<T> {
			var index = -1
			override fun hasNext(): Boolean {
				return index < 0 && get() != null
			}

			override fun next(): T {
				index++
				return get()!!
			}
		}
	}
}

/**
 * non-mutating
 */
fun <T> Cell<T>.intoSingleCell(): SingleCell<T> = SingleCell(this.get())