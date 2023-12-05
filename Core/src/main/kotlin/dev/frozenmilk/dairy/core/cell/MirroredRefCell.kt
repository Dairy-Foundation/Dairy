package dev.frozenmilk.dairy.core.cell

import java.lang.reflect.Field

@Suppress("UNCHECKED_CAST")
open class MirroredRefCell<T>(
        private val parent: Any,
        private val field: Field,
) : SafeCell<T>(null) // we actually don't care about the ref in this circumstance
{
    init {
        field.isAccessible = true;
    }

    override fun get(): T {
        return this.field.get(parent) as T;
    }

    override fun accept(p0: T?) {
        this.field.set(parent, p0)
    }
}
