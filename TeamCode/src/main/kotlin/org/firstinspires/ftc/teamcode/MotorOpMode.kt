package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.ZeroPowerBehaviour
import dev.frozenmilk.dairy.calcified.hardware.controller.Controller
import dev.frozenmilk.dairy.calcified.hardware.controller.PController
import dev.frozenmilk.dairy.calcified.geometry.angle.AngleRadians
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.cell.OpModeLazyCell
import dev.frozenmilk.util.cell.RefCell
import dev.frozenmilk.util.cell.getValue

// enables all dairy features
@DairyCore
@TeleOp
class MotorOpMode : OpMode() {
	init {
		// ensures that the feature flags to enable the MarrowMap are present, otherwise throws a helpful error
		FeatureRegistrar.checkFeatures(Calcified)
	}

	val motor: CalcifiedMotor by dev.frozenmilk.util.cell.LazyCell {
		Calcified.controlHub.motors.getMotor(0)
	}

	val motor1: CalcifiedMotor by dev.frozenmilk.util.cell.OpModeLazyCell {
		Calcified.controlHub.motors.getMotor(1)
	}

	val motorController: Controller<AngleRadians> by lazy {
		PController(
				MotorControllerGroup(listOf(motor)),
				Calcified.controlHub.encoders.getRadiansEncoder(0, 8192.0).positionSupplier,
				0.1
		)
	}

	override fun init() {
		motor.zeroPowerBehavior = ZeroPowerBehaviour.BRAKE

		motorController.update(AngleRadians())
	}

	override fun loop() {
		Calcified.controlHub.refreshBulkCache()
		if (gamepad1.a) motor.power = 1.0
		else if (gamepad1.b) motor.power = -1.0
		else motor.power = 0.0
	}
}