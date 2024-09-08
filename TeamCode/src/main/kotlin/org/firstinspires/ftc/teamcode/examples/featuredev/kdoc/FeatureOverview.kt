package org.firstinspires.ftc.teamcode.examples.featuredev.kdoc

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.wrapper.Wrapper

class FeatureOverview : Feature {
	init {
		// true if this is currently active
		// true means it will receive updates for the current OpMode
		val isActive: Boolean = active
	}

	// we won't look at the dependency system closely here
	override var dependency: Dependency<*> = Dependency { opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean -> }

	//
	// Hooks
	//

	// By default, all the hooks are empty, so you only need to override the ones you want to use

	override fun preUserInitHook(opMode: Wrapper) {}

	override fun postUserInitHook(opMode: Wrapper) {}

	override fun preUserInitLoopHook(opMode: Wrapper) {}

	override fun postUserInitLoopHook(opMode: Wrapper) {}

	override fun preUserStartHook(opMode: Wrapper) {}

	override fun postUserStartHook(opMode: Wrapper) {}

	override fun preUserLoopHook(opMode: Wrapper) {}

	override fun postUserLoopHook(opMode: Wrapper) {}

	override fun preUserStopHook(opMode: Wrapper) {}

	override fun postUserStopHook(opMode: Wrapper) {}

	// cleanup differs from postUserStopHook, it runs after the OpMode has completely stopped,
	// and is guaranteed to run, even if the OpMode stopped from a crash.
	override fun cleanup(opMode: Wrapper) {}

	init {
		// finally, lets look at some Feature related FeatureRegistrar methods

		FeatureRegistrar.activeFeatures // list of currently active features
		FeatureRegistrar.registeredFeatures // list of registered features

		FeatureRegistrar.isFeatureActive(this) // boolean, same as Feature.isActive()

		// don't register and deregister Features a lot, its expensive
		// try to keep this to only during construction / init, or only one or two at runtime
		// the more you do, the more expensive it is
		FeatureRegistrar.registerFeature(this) // same as Feature.register()
		FeatureRegistrar.deregisterFeature(this) // same as Feature.deregister()
	}
}