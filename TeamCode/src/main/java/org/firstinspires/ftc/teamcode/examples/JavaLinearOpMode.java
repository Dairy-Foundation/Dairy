package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import dev.frozenmilk.dairy.core.FeatureRegistrar;

// add feature annotations here
public class JavaLinearOpMode extends LinearOpMode {
	@Override
	public void runOpMode() {
		// unfortunately, linear we can't do things so automatically with LinearOpModes
		// so you need to call the hooks your self
		// failing to wrap your code in these lines will likely cause issues

		// DO NOT put code before this
		FeatureRegistrar.checkFeatures(this /* pass desired features as varargs here */);
		FeatureRegistrar.onOpModePreInit(FeatureRegistrar.getActiveOpMode());
		// your init code here
		// remember that you can use OpModeLazyCells to init your hardware and similar
		FeatureRegistrar.onOpModePostInit(FeatureRegistrar.getActiveOpMode());
		while (opModeInInit()) {
			FeatureRegistrar.onOpModePreInitLoop(FeatureRegistrar.getActiveOpMode());
			// your init_loop code here
			FeatureRegistrar.onOpModePostInitLoop(FeatureRegistrar.getActiveOpMode());
		}
		waitForStart();
		FeatureRegistrar.onOpModePreStart(FeatureRegistrar.getActiveOpMode());
		// your start code here
		FeatureRegistrar.onOpModePostStart(FeatureRegistrar.getActiveOpMode());
		while (opModeIsActive()) {
			FeatureRegistrar.onOpModePreLoop(FeatureRegistrar.getActiveOpMode());
			// your loop code here
			FeatureRegistrar.onOpModePostLoop(FeatureRegistrar.getActiveOpMode());
		}
		FeatureRegistrar.onOpModePreStop(FeatureRegistrar.getActiveOpMode());
		// your stop code here
		FeatureRegistrar.onOpModePostStop(FeatureRegistrar.getActiveOpMode());

		// obviously this is much more ugly, but there is no way to automate this for you
		// so we advise that iterative OpModes are used instead
	}
}