package dev.frozenmilk.dairy.ftclink.calcified.hardware

import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
import com.qualcomm.robotcore.hardware.DcMotor
import dev.frozenmilk.dairy.ftclink.calcified.CalcifiedModule
import kotlin.math.abs

open class CalcifiedMotor internal constructor(private val module: CalcifiedModule, private val port: Byte) {
	var direction = Direction.FORWARD
	var cachingTolerance = 0.02
	var enabled = true
		set(value) {
			if (field != value) {
				// sends the command to change the enable state
				LynxSetMotorChannelEnableCommand(module.lynxModule, port.toInt(), field).send()
				field = value
			}
		}

	var zeroPowerBehavior = ZeroPowerBehaviour.FLOAT
		set(value) {
			if (field != zeroPowerBehavior) {
				// sets the command to change the 0 power behaviour
				LynxSetMotorChannelModeCommand(module.lynxModule, port.toInt(), DcMotor.RunMode.RUN_WITHOUT_ENCODER, value.wrapping)
				field = value
			}
		}

	var power = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				LynxSetMotorConstantPowerCommand(module.lynxModule, port.toInt(), (correctedValue * LynxSetMotorConstantPowerCommand.apiPowerLast).toInt()).send()
				field = correctedValue
			}
		}

	// todo add current and other features of the motor in here, so we aren't limiting people
}