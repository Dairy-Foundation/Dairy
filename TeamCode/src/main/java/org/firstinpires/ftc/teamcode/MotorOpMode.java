package org.firstinpires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import dev.frozenmilk.dairy.ftclink.calcified.MarrowMap;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.Controller;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.PController;
import dev.frozenmilk.dairy.ftclink.geometry.angle.Angle;
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleDegrees;
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleRadians;

public class MotorOpMode extends OpMode {
	private CalcifiedMotor motor;
	private Controller<AngleRadians> controller;
	
	@Override
	public void init() {
		motor = MarrowMap.INSTANCE.getControlHub().getMotors().getMotor((byte) 0);
		
		controller = new PController<>(
				new MotorControllerGroup(motor),
				MarrowMap.INSTANCE.getControlHub().getEncoders()
						.getRadiansEncoder((byte) 0, 8192).getPositionSupplier(),
				0.5
		);
	}
	
	@Override
	public void loop() {
		Angle setPoint;
		if (gamepad1.a) {
			setPoint = new AngleDegrees(180);
		} else {
			setPoint = new AngleDegrees(0);
		}
		
		controller.update(setPoint.intoRadians());
	}
}



