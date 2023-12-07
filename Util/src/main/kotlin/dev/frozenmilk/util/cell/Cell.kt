package dev.frozenmilk.util.cell

import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.reflect.KProperty

/**
 * a top level cell interface, cells act like pointers, and recreate many features of them
 */
interface Cell<T> : Consumer<T>, Supplier<T>

operator fun <T> Cell<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()
operator fun <T> Cell<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = accept(value)