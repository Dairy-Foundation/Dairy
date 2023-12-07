package dev.frozenmilk.util.cell

import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.Dependency
import dev.frozenmilk.dairy.core.DependencySet
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import java.util.function.Supplier

/**
 * A [LazyCell] that is initialised on the init of an OpMode
 */
class OpModeLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier) , Feature{
	init {
		FeatureRegistrar.registerFeature(this)
	}

	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.includesAtLeastOneOf(DairyCore::class.java)

	override fun preUserInitHook(opMode: OpModeWrapper) {
		get()
	}

	override fun postUserInitHook(opMode: OpModeWrapper) {}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStartHook(opMode: OpModeWrapper) {}

	override fun postUserStartHook(opMode: OpModeWrapper) {}

	override fun preUserLoopHook(opMode: OpModeWrapper) {}

	override fun postUserLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStopHook(opMode: OpModeWrapper) {}

	override fun postUserStopHook(opMode: OpModeWrapper) {
		FeatureRegistrar.deregisterFeature(this)
	}
}