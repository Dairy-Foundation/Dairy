package collections

import datacarton.DataLine
import kotlin.math.max

@Suppress("unused")
class DataLineArrayList<E : DataLine?> : ArrayList<E?> {
    var labelWidth = 0
        private set

    constructor(initialCapacity: Int) : super(initialCapacity)
    constructor() : super()
    constructor(c: Collection<E>) : super(c)

    private fun findLabelWidth() {
        this.forEach {
            it?.let {
                labelWidth = max(labelWidth, it.labelWidth);
            }
        }
    }

    private fun safeAdd(incoming: E?) {
        incoming?.let {
            labelWidth = max(incoming.labelWidth, labelWidth);
        }
    }

    private fun safeRemove(outgoing: E?) {
        outgoing?.let {
            if (outgoing.labelWidth == labelWidth) findLabelWidth();
        }
    }

    override fun set(index: Int, element: E?): E? {
        val outgoing = this[index]
        val operation: E? = super.set(index, element)
        safeRemove(outgoing)
        safeAdd(element)
        return operation
    }

    override fun add(element: E?): Boolean {
        safeAdd(element)
        return super.add(element)
    }

    override fun add(index: Int, element: E?) {
        safeAdd(element)
        super.add(index, element)
    }

    override fun addAll(elements: Collection<E?>): Boolean {
        val operation = super.addAll(elements)
        findLabelWidth()
        return operation
    }

    override fun addAll(index: Int, elements: Collection<E?>): Boolean {
        val operation = super.addAll(index, elements)
        findLabelWidth()
        return operation
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        findLabelWidth()
    }
}
