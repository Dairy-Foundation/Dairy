package org.firstinspires.ftc.teamcode.examples;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import dev.frozenmilk.dairy.core.FeatureRegistrar;

// add feature annotations here
public class JavaIterativeOpMode extends OpMode {
	public JavaIterativeOpMode() {
		FeatureRegistrar.checkFeatures(this /* pass desired features as varargs here */);
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