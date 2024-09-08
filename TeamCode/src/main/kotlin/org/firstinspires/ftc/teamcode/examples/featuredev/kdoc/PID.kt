package org.firstinspires.ftc.teamcode.examples.featuredev.kdoc

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper

class PID constructor(/* vals/vars for encoder, motor, coefficients... */): Feature {
	// first, we need to set up the dependency
	// Yielding just says "this isn't too important, always attach me, but run me after more important things"
	// Yielding is reusable!
	override var dependency: Dependency<*> = Yielding

	init {
		// regardless of constructor used, call register when the class is instantiated
		register()
	}

	private fun update() {
		// calculate next output using encoder, target and coefficients

		// don't update motor power if the controller isn't enabled
		if (!isEnabled) return

		// set motor power to calculated output
	}

	// users should be able to change the target
	var target: Int = 0

	// users should be able to enable / disable the controller
	var isEnabled: Boolean = true

	// after init loop and loop we will update the controller
	override fun postUserInitLoopHook(opMode: Wrapper) {
		update()
	}

	override fun postUserLoopHook(opMode: Wrapper) {
		update()
	}

	// in cleanup we deregister, which prevents this from sticking around for another OpMode,
	// unless the user calls register again
	override fun cleanup(opMode: Wrapper) {
		deregister()
	}
}