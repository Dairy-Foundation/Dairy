package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.ZeroPowerBehaviour
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeLazyCell

// enables all dairy features
@DairyCore
@TeleOp
class MotorOpMode : OpMode() {

	val motor: CalcifiedMotor by OpModeLazyCell {
		Calcified.controlHub.motors.getMotor(0)
	}
//
//	val motor1: CalcifiedMotor by OpModeLazyCell {
//		Calcified.controlHub.motors.getMotor(1)
//	}
//
//	val motorController: Controller<AngleRadians> by OpModeLazyCell{
//		PController(
//				MotorControllerGroup(listOf(motor)),
//				Calcified.controlHub.encoders.getRadiansEncoder(0, 8192.0).positionSupplier,
//				0.1
//		)
//	}

	init {
		// ensures that the feature flags to enable the MarrowMap are present, otherwise throws a helpful error
		FeatureRegistrar.checkFeatures(this, Calcified)
	}

	override fun init() {
//		OpModeLazyCell {}
		Calcified.controlHub.motors.getMotor(0).zeroPowerBehavior = ZeroPowerBehaviour.BRAKE

//		motorController.update(AngleRadians())
	}

	override fun loop() {
//		if (gamepad1.a) motor.power = 1.0
//		else if (gamepad1.b) motor.power = -1.0
//		else motor.power = 0.0
	}
}