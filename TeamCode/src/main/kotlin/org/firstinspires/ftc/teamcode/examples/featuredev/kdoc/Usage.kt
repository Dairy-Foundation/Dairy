package org.firstinspires.ftc.teamcode.examples.featuredev.kdoc

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.FeatureRegistrar.checkFeatures
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import org.firstinspires.ftc.teamcode.examples.featuredev.jdoc.BulkReads

// we add this, and BulkReads will receive updates for this OpMode
// which means it will handle bulk reads for us!
@BulkReads.Attach
// this annotation makes it so that the FeatureRegistrar will log all the reasons
// for any registered Features that weren't activated
@FeatureRegistrar.LogDependencyResolutionExceptions
class Usage : OpMode() {
	init {
		// instead of `@FeatureRegistrar.LogDependencyResolutionExceptions`
		// checkFeatures can be used to ensure that all features
		// passed to the function will be activated,
		// or will throw an error for them specifically
		// both are good debugging tools!
		checkFeatures(BulkReads.INSTANCE /*, varargs Features*/)
	}

	// we'll look at OpModeLazyCell later, but this means that this PID will be instantiated in init for us
	// for this example it doesn't really matter, but if we actually implemented it, then we would need to use this
	// to ensure that we don't access the hardware map until init
	private val pid by OpModeLazyCell { PID() }
	// if you're new to Kotlin, the `by` keyword is called delegation

	override fun init() {
	}

	// we can set the target in loop if we want
	// and we don't need to worry about anything else!
	override fun loop() {
		if (gamepad1.a) {
			pid.target = 100
		} else if (gamepad1.b) {
			pid.target = 0
		}
	}
}
