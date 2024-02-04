package org.firstinspires.ftc.teamcode.examples

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import dev.frozenmilk.dairy.core.FeatureRegistrar

// add feature annotations here
class KotlinLinearOpMode : LinearOpMode() {
	override fun runOpMode() {
		// unfortunately, linear we can't do things so automatically with LinearOpModes
		// so you need to call the hooks your self
		// failing to wrap your code in these lines will likely cause issues

		// DO NOT put code before this
		FeatureRegistrar.checkFeatures(this, /* pass desired features as varargs here */)
		FeatureRegistrar.opModePreInit(FeatureRegistrar.activeOpModeWrapper!!)
		// your init code here
		// remember that you can use OpModeLazyCells to init your hardware and similar
		FeatureRegistrar.opModePostInit(FeatureRegistrar.activeOpModeWrapper!!)
		while (opModeInInit()) {
			FeatureRegistrar.onOpModePreInitLoop(FeatureRegistrar.activeOpModeWrapper!!)
			// your init_loop code here
			FeatureRegistrar.onOpModePostInitLoop(FeatureRegistrar.activeOpModeWrapper!!)
		}
		waitForStart()
		FeatureRegistrar.onOpModePreStart(FeatureRegistrar.activeOpModeWrapper!!)
		// your start code here
		FeatureRegistrar.onOpModePostStart(FeatureRegistrar.activeOpModeWrapper!!)
		while (opModeIsActive()) {
			FeatureRegistrar.onOpModePreLoop(FeatureRegistrar.activeOpModeWrapper!!)
			// your loop code here
			FeatureRegistrar.onOpModePostLoop(FeatureRegistrar.activeOpModeWrapper!!)
		}
		FeatureRegistrar.onOpModePreStop(FeatureRegistrar.activeOpModeWrapper!!)
		// your stop code here
		FeatureRegistrar.onOpModePostStop(FeatureRegistrar.activeOpModeWrapper!!)

		// obviously this is much more ugly, but there is no way to automate this for you
		// so we advise that iterative OpModes are used instead
	}
}