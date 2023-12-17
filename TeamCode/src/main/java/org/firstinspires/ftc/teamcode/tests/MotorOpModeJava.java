package org.firstinspires.ftc.teamcode.tests;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.frozenmilk.dairy.calcified.Calcified;
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor;
import dev.frozenmilk.dairy.core.DairyCore;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.OpModeLazyCell;

@DairyCore
@TeleOp
public class MotorOpModeJava extends OpMode {
	public MotorOpModeJava() {
		FeatureRegistrar.INSTANCE.checkFeatures(this, Calcified.INSTANCE);
	}
	
	private final OpModeLazyCell<CalcifiedMotor> motor = new OpModeLazyCell<>(() -> Calcified.INSTANCE.getControlHub().getMotors().getMotor((byte) 1));

	@Override
	public void init() {
	}

	@Override
	public void loop() {
		motor.get().setPower(Math.sin(getRuntime()));
	}
}
