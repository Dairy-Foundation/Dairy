package org.firstinspires.ftc.teamcode.tests

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.core.util.OpModeLazyCell

@TeleOp
@Calcified.Attach
class FeatureRegistrarTest : OpMode() {
	val servo by OpModeLazyCell {
		Calcified.controlHub.getServo(2)
	}
	val crservo by OpModeLazyCell {
		Calcified.controlHub.getContinuousServo(1)
	}
	val motor by OpModeLazyCell {
		Calcified.controlHub.getMotor(0)
	}
	val encoder by OpModeLazyCell {
		Calcified.controlHub.getTicksEncoder(0)
	}
	val imu by OpModeLazyCell {
		Calcified.controlHub.getIMU(0)
	}

	override fun init() {

	}

	override fun loop() {
		telemetry.addData("e pos", encoder.position)
		telemetry.addData("e vel", encoder.velocity)

		telemetry.addData("heading", imu.heading)
		telemetry.addData("heading velo", imu.headingVelocity)

		if (Calcified.gamepad1.a.whenTrue) {
			servo.position = 1.0
			crservo.power = 1.0
			motor.power = 1.0
		}

		if (Calcified.gamepad1.b.whenTrue) {
			servo.position = 0.0
			crservo.power = 0.0
			motor.power = 0.0
		}
	}
}