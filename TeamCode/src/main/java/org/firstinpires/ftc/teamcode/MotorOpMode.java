package org.firstinpires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.Collections;

import dev.frozenmilk.dairy.ftclink.apputil.DairyCore;
import dev.frozenmilk.dairy.ftclink.apputil.EventRegistrar;
import dev.frozenmilk.dairy.ftclink.calcified.MarrowMap;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.Controller;
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.PController;
import dev.frozenmilk.dairy.ftclink.geometry.angle.Angle;
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleDegrees;
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleRadians;

/**
 * enables all dairy features
 */
@DairyCore
@TeleOp(name = "MotorOpMode (Java)")
public class MotorOpMode extends OpMode {
	private CalcifiedMotor motor;
	private Controller<AngleRadians> controller;
	
	@Override
	public void init() {
		// ensures that the feature flags to enable the MarrowMap are present, otherwise throws a helpful error
		EventRegistrar.INSTANCE.checkFeatures(MarrowMap.INSTANCE);
		
		motor = MarrowMap.INSTANCE.getControlHub().getMotors().getMotor((byte) 0);
		
		controller = new PController<>(
				new MotorControllerGroup(Collections.singleton(motor)),
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



