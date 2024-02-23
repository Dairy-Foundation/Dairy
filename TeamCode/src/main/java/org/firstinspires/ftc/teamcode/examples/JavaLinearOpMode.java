package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.Objects;

import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.util.units.angle.AngleUnit;

// add feature annotations here
public class JavaLinearOpMode extends LinearOpMode {
	@Override
	public void runOpMode() {
		Wrapper activeOpModeWrapper = Objects.requireNonNull(FeatureRegistrar.getActiveOpModeWrapper());
		// unfortunately, linear we can't do things so automatically with LinearOpModes
		// so you need to call the hooks your self
		// failing to wrap your code in these lines will likely cause issues

		// DO NOT put code before this
		FeatureRegistrar.opModePreInit(activeOpModeWrapper);
		// your init code here
		// remember that you can use OpModeLazyCells to init your hardware and similar
		FeatureRegistrar.opModePostInit(activeOpModeWrapper);
		while (opModeInInit()) {
			FeatureRegistrar.opModePreInitLoop(activeOpModeWrapper);
			// your init_loop code here
			FeatureRegistrar.opModePostInitLoop(activeOpModeWrapper);
		}
		waitForStart();
		FeatureRegistrar.opModePreStart(activeOpModeWrapper);
		// your start code here
		FeatureRegistrar.opModePostStart(activeOpModeWrapper);
		while (opModeIsActive()) {
			FeatureRegistrar.opModePreLoop(activeOpModeWrapper);
			// your loop code here
			FeatureRegistrar.opModePostLoop(activeOpModeWrapper);
		}
		FeatureRegistrar.opModePreStop(activeOpModeWrapper);
		// your stop code here
		FeatureRegistrar.opModePostStop(activeOpModeWrapper);

		// obviously this is much more ugly, but there is no way to automate this for you
		// so we advise that iterative OpModes are used instead
	}
}