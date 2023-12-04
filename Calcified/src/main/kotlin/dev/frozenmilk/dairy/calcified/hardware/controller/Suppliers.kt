package dev.frozenmilk.dairy.calcified.hardware.controller

import java.util.function.Supplier

@FunctionalInterface
interface ErrorSupplier<IN, OUT> {
	fun getError(target: IN): OUT
	fun <PIPE> pipe(pipe: ErrorSupplier<OUT, PIPE>): ErrorSupplier<IN, PIPE> {
		val self = this
		if (self == pipe) throw IllegalArgumentException("Cannot pipe an ErrorSupplier into itself")
		return object : ErrorSupplier<IN, PIPE> {
			override fun getError(target: IN): PIPE {
				return pipe.getError(self.getError(target))
			}
		}
	}
}

/**
 * capable of supplying both position and error
 */
interface CompoundSupplier<UNIT, ERROR> : ErrorSupplier<UNIT, ERROR>, Supplier<UNIT>

/**
 * [clearCache] will be called once / cycle to allow this version of the compound supplier to cache results
 *
 * generally only the [CompoundSupplier] component should be made publicly available, as [clearCache] should be called by a [dev.frozenmilk.dairy.ftclink.apputil.Feature]
 *
 * @see[CompoundSupplier]
 */
interface CachedCompoundSupplier<UNIT, ERROR> : CompoundSupplier<UNIT, ERROR> {
	fun clearCache()
}
