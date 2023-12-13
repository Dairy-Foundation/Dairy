package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import kotlin.math.abs

class CalcifiedMotor internal constructor(private val module: CalcifiedModule, private val port: Byte) : ComplexMotor {
	override var direction = Direction.FORWARD
	override var cachingTolerance = 0.005
	override var enabled = true
		set(value) {
			if (field != value) {
				// sends the command to change the enable state
				LynxSetMotorChannelEnableCommand(module.lynxModule, port.toInt(), value).send()
				field = value
			}
		}

	override var zeroPowerBehaviour = ZeroPowerBehaviour.FLOAT
		set(value) {
			if (field != value) {
				// sets the command to change the 0 power behaviour
				LynxSetMotorChannelModeCommand(module.lynxModule, port.toInt(), DcMotor.RunMode.RUN_WITHOUT_ENCODER, value.wrapping)
				field = value
			}
		}

	override var power = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				LynxSetMotorConstantPowerCommand(module.lynxModule, port.toInt(), (correctedValue * LynxSetMotorConstantPowerCommand.apiPowerLast).toInt()).send()
				field = correctedValue
			}
		}

	override fun getCurrent(unit: CurrentUnit): Double {
		return unit.convert(LynxGetADCCommand(module.lynxModule, LynxGetADCCommand.Channel.motorCurrent(port.toInt()), LynxGetADCCommand.Mode.ENGINEERING).sendReceive().value.toDouble(), CurrentUnit.MILLIAMPS)
	}
}

