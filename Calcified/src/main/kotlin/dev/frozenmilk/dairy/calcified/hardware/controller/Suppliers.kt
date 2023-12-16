package dev.frozenmilk.dairy.calcified.hardware.controller

import java.util.function.Supplier

interface ErrorSupplier<IN, OUT> {
	fun getError(target: IN): OUT
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
