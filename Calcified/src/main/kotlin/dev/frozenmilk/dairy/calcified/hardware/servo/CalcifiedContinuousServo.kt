package dev.frozenmilk.dairy.calcified.hardware.servo

import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMConfigurationCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoPulseWidthCommand
import com.qualcomm.robotcore.hardware.PwmControl
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import kotlin.math.abs

class CalcifiedContinuousServo internal constructor(private val module: CalcifiedModule, private val port: Byte) : SimpleMotor,
	PWMDevice {
	override var direction: Direction = Direction.FORWARD
	override var pwmRange: PwmControl.PwmRange = PwmControl.PwmRange(600.0, 2400.0)
		set(value) {
			if (value.usFrame != field.usFrame) {
				LynxSetPWMConfigurationCommand(module.lynxModule, port.toInt(), value.usFrame.toInt()).send()
			}
			field = value
		}
	override var cachingTolerance: Double = 0.005
	override var enabled: Boolean = true
		set(value) {
			if (field != value) {
				// sends the command to change the enable state
				LynxSetServoEnableCommand(module.lynxModule, port.toInt(), value).send()
				field = value
			}
		}

	override var power: Double = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			if (!enabled) return
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				val pwm = Range
						.scale(correctedValue, -1.0, 1.0, pwmRange.usPulseLower, pwmRange.usPulseUpper)
						.toInt()
						.coerceIn(LynxSetServoPulseWidthCommand.apiPulseWidthFirst, LynxSetServoPulseWidthCommand.apiPulseWidthLast)

				LynxSetServoPulseWidthCommand(module.lynxModule, port.toInt(), pwm).send()
				field = correctedValue
			}
		}
}