package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import dev.frozenmilk.dairy.calcified.Calcified;
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedMotor;
import dev.frozenmilk.dairy.calcified.hardware.ZeroPowerBehaviour;
import dev.frozenmilk.dairy.core.DairyCore;
import dev.frozenmilk.dairy.core.collections.cell.OpModeLazyCell;

@DairyCore
@TeleOp
public class MotorOpModeJava extends OpMode {
	private final OpModeLazyCell<CalcifiedMotor> motor = new OpModeLazyCell<>(() -> {
		CalcifiedMotor result = Calcified.INSTANCE.getControlHub().getMotors().getMotor((byte) 1);
		result.setCachingTolerance(0.02);
		result.setZeroPowerBehavior(ZeroPowerBehaviour.FLOAT);
		return result;
	});
	
	@Override
	public void init() {
		motor.get().setPower(1.0);
	}
	
	@Override
	public void loop() {
	
	}
}
