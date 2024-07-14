package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import dev.frozenmilk.dairy.core.FeatureRegistrar;

// add feature annotations here
public class JavaIterativeOpMode extends OpMode {
	// this block is optional, but may be helpful for debugging,
	// first, check the logcat files to see if your feature was:
	// 1) if the feature is a global object: located by FeatureSinisterFilter during startup
	// 2) if the feature is located, but you think it didn't attach, check to see if it was activated for this OpMode
	// 3) if the feature is located, and you have confirmed that, add this block to get debug messages about why it did not
	public JavaIterativeOpMode() {
		FeatureRegistrar.checkFeatures(/* pass desired features as varargs here */);
	}

	@Override
	public void init() {
		// the rest is as normal
		// remember that you can use OpModeLazyCells to init your hardware and similar
	}

	@Override
	public void init_loop() {
		// the rest is as normal
	}

	@Override
	public void start() {
		// the rest is as normal
	}

	@Override
	public void loop() {
		// the rest is as normal
	}

	@Override
	public void stop() {
		// the rest is as normal
	}
}