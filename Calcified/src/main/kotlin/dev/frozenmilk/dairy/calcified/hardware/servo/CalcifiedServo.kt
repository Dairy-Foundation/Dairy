package dev.frozenmilk.dairy.calcified.hardware.servo

import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMConfigurationCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoPulseWidthCommand
import com.qualcomm.robotcore.hardware.PwmControl.PwmRange
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import kotlin.math.abs

class CalcifiedServo internal constructor(private val module: CalcifiedModule, private val port: Byte) : PWMDevice {
	var direction = Direction.FORWARD;

	override var pwmRange: PwmRange = PwmRange.defaultRange
		set(value) {
			if (value.usFrame != field.usFrame) {
				LynxSetPWMConfigurationCommand(module.lynxModule, port.toInt(), value.usFrame.toInt()).send()
			}
			field = value
		}
	var cachingTolerance = 0.001

	var enabled = true
		set(value) {
			if (field != value) {
				LynxSetServoEnableCommand(module.lynxModule, port.toInt(), value).send()
				field = value;
			}
		}

	var position = Double.NaN
		set(value) {
			if (!enabled) return
			var correctedValue = value.coerceIn(0.0, 1.0)
			if (direction == Direction.REVERSE) correctedValue = 1 - correctedValue
			if (field.isNaN() || abs(field - correctedValue) >= cachingTolerance || correctedValue == 0.0 && field != 0.0 || correctedValue == 1.0 && field != 1.0) {
				val pwm = Range
						.scale(correctedValue, 0.0, 1.0, pwmRange.usPulseLower, pwmRange.usPulseUpper)
						.toInt()
						.coerceIn(LynxSetServoPulseWidthCommand.apiPulseWidthFirst, LynxSetServoPulseWidthCommand.apiPulseWidthLast)

				LynxSetServoPulseWidthCommand(module.lynxModule, port.toInt(), pwm).send()
				field = correctedValue
			}
		}

	/**
	 * sets the position, ignoring caching tolerance
	 */
	fun forcePosition(position: Double) {
		val tolerance = cachingTolerance
		cachingTolerance = 0.0
		this.position = position
		cachingTolerance = tolerance
	}
}
