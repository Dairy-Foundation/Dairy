package dev.frozenmilk.util.cell

import java.lang.reflect.Field

/**
 * a cell that manages a reference through reflection
 */
@Suppress("UNCHECKED_CAST")
open class MirroredCell<T>(
        private val parent: Any,
        private val field: Field,
) : LateInitCell<T>() // we actually don't care about the ref in this circumstance
{
    constructor(parent: Any, field: String) : this(parent, recurseFindField(parent::class.java, field))
    init {
        field.isAccessible = true;
    }

    override fun get(): T {
        return this.field.get(parent) as T;
    }

    override fun accept(p0: T) {
        this.field.set(parent, p0)
    }
}

fun recurseFindField(clazz: Class<*>?, field: String): Field {
    if (clazz == null) throw IllegalStateException("unable to find field after searching all classes and super classes")
    return try {
        clazz.getDeclaredField(field)
    } catch (_: Exception) {
        recurseFindField(clazz.superclass, field)
    }
}
