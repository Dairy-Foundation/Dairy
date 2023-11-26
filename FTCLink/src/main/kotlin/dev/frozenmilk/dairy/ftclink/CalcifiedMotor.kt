package dev.frozenmilk.dairy.ftclink

import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import kotlin.math.abs

class CalcifiedMotor internal constructor(private val module: CalcifiedModule, private val port: Byte) {
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

	var zeroPowerBehavior = ZeroPowerBehavior.FLOAT
		set(value) {
			if (field != zeroPowerBehavior) {
				// sets the command to change the 0 power behaviour
				LynxSetMotorChannelModeCommand(module.lynxModule, port.toInt(), DcMotor.RunMode.RUN_WITHOUT_ENCODER, value)
				field = value
			}
		}

	var power = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			value.coerceIn(-1.0, 1.0)
			if (abs(field - value) >= cachingTolerance || (value >= 1.0 && field != 1.0) || (value <= -1.0 && field != -1.0)) {
				val power = value * direction.multiplier
				LynxSetMotorConstantPowerCommand(module.lynxModule, port.toInt(), (power * LynxSetMotorConstantPowerCommand.apiPowerLast).toInt()).send()
				field = power
			}
		}

	enum class Direction(val multiplier: Byte) {
		FORWARD(1),
		REVERSE(-1)
	}
}