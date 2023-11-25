package collections.reinitialisable

import java.lang.reflect.Field

@Suppress("UNCHECKED_CAST")
/**
 * marked private for the moment
 */
private class MirroredRefCell<T>(
        private val parent: Any,
        private val field: Field,
) {
    init {
        field.isAccessible = true;
    }

    fun get(): T {
        return this.field.get(parent) as T;
    }

    fun set(value: T) {
        this.field.set(parent, value)
    }

    override fun toString(): String = get().toString()
}
