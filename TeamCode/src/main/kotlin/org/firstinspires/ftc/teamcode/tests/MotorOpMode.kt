package org.firstinspires.ftc.teamcode.tests

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import dev.frozenmilk.dairy.core.util.cachinghardwaredevice.CachingDcMotorEX
import kotlin.math.sin

// enables all dairy features
@DairyCore
@TeleOp
class MotorOpMode : OpMode() {

	private val motor: CalcifiedMotor by OpModeLazyCell {
		Calcified.controlHub.motors.getMotor(0)
	}

	init {
		// ensures that the feature flags to enable Calcified are present, otherwise throws a helpful error
		FeatureRegistrar.checkFeatures(this, Calcified)
	}

	override fun init() {
		val test = CachingDcMotorEX(hardwareMap.get(DcMotorEx::class.java, "b"))
	}

	override fun loop() {
		Calcified.controlHub.encoders.getTicksEncoder(0).clearCache()
		motor.power = sin(runtime)
	}
}