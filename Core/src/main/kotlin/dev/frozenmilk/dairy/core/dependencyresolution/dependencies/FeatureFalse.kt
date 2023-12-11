package dev.frozenmilk.dairy.core.dependencyresolution.dependencies

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet

/**
 * a dummy feature that is returned instead of a nullable outcome, this should never actually be returned to the consumer, as this should only be returned alongside a failed resolution, which then throws an error
 */
internal object FeatureFalse : Feature {
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this).yields()

	override fun preUserInitHook(opMode: OpModeWrapper) {}

	override fun postUserInitHook(opMode: OpModeWrapper) {}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStartHook(opMode: OpModeWrapper) {}

	override fun postUserStartHook(opMode: OpModeWrapper) {}

	override fun preUserLoopHook(opMode: OpModeWrapper) {}

	override fun postUserLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStopHook(opMode: OpModeWrapper) {}

	override fun postUserStopHook(opMode: OpModeWrapper) {}
}