package org.firstinspires.ftc.teamcode.examples.templating;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
// because we are extending JavaTemplate, we get everything we set up before!
public class JavaTemplatedOpMode extends JavaTemplate {
	@Override
	public void init() {
	}

	@Override
	public void loop() {
		getLeftBack().setPower(1.0);
		// etc...
	}
}
