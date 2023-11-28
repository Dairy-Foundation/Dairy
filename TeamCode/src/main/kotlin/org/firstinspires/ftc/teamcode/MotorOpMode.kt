package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.ftclink.calcified.MarrowMap
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup
import dev.frozenmilk.dairy.ftclink.calcified.hardware.RadiansEncoder
import dev.frozenmilk.dairy.ftclink.calcified.hardware.ZeroPowerBehaviour
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.Controller
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.PController
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleRadians

@TeleOp
class MotorOpMode : OpMode() {
	val motor: CalcifiedMotor by lazy {
		MarrowMap.controlHub.motors.getMotor(0)
	}

	val motorController: Controller<AngleRadians> by lazy {
		PController(
				MotorControllerGroup(motor),
				MarrowMap.controlHub.encoders.getRadiansEncoder(0, 8192.0).positionSupplier,
				0.1
		)
	}

	override fun init() {
		motor.zeroPowerBehavior = ZeroPowerBehaviour.BRAKE


		motorController.update(AngleRadians())
	}

	override fun loop() {
		MarrowMap.controlHub.refreshBulkCache()
		if (gamepad1.a) motor.power = 1.0
		else if (gamepad1.b) motor.power = -1.0
		else motor.power = 0.0
	}
}