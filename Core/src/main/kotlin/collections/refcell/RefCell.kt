package collections.refcell

import datacarton.annotations.Data
import datacarton.annotations.Flatten
import java.util.function.Consumer
import java.util.function.Supplier

open class RefCell<T>(private var ref: T) : Supplier<T>, Consumer<T> {
    override fun get(): T {
        return ref
    }

    override fun accept(p0: T) {
        this.ref = p0;
    }
}