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
		FeatureRegistrar.onOpModePreInit(FeatureRegistrar.activeOpMode!!)
		// your init code here
		// remember that you can use OpModeLazyCells to init your hardware and similar
		FeatureRegistrar.onOpModePostInit(FeatureRegistrar.activeOpMode!!)
		while (opModeInInit()) {
			FeatureRegistrar.onOpModePreInitLoop(FeatureRegistrar.activeOpMode!!)
			// your init_loop code here
			FeatureRegistrar.onOpModePostInitLoop(FeatureRegistrar.activeOpMode!!)
		}
		waitForStart()
		FeatureRegistrar.onOpModePreStart(FeatureRegistrar.activeOpMode!!)
		// your start code here
		FeatureRegistrar.onOpModePostStart(FeatureRegistrar.activeOpMode!!)
		while (opModeIsActive()) {
			FeatureRegistrar.onOpModePreLoop(FeatureRegistrar.activeOpMode!!)
			// your loop code here
			FeatureRegistrar.onOpModePostLoop(FeatureRegistrar.activeOpMode!!)
		}
		FeatureRegistrar.onOpModePreStop(FeatureRegistrar.activeOpMode!!)
		// your stop code here
		FeatureRegistrar.onOpModePostStop(FeatureRegistrar.activeOpMode!!)

		// obviously this is much more ugly, but there is no way to automate this for you
		// so we advise that iterative OpModes are used instead
	}
}