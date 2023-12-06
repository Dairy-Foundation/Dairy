package dev.frozenmilk.dairy.core.collections.cell

import java.util.function.Consumer
import java.util.function.Supplier

/**
 * a top level cell interface, cells act like pointers, and recreate many features of them
 */
interface Cell<T> : Consumer<T>, Supplier<T>