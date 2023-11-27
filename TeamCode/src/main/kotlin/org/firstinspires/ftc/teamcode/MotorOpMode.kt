package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.ftclink.calcified.MarrowMap
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.ftclink.calcified.hardware.ZeroPowerBehaviour

@TeleOp
class MotorOpMode : OpMode() {
	val motor: CalcifiedMotor by lazy {
		MarrowMap.controlHub.getMotor(0)
	}

	override fun init() {
		motor.zeroPowerBehavior = ZeroPowerBehaviour.BRAKE
	}

	override fun loop() {
		MarrowMap.controlHub.refreshBulkCache()
		if (gamepad1.a) motor.power = 1.0
		else if (gamepad1.b) motor.power = -1.0
		else motor.power = 0.0
	}
}