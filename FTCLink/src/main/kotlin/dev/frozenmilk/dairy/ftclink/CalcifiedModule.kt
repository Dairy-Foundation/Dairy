package dev.frozenmilk.dairy.ftclink

import com.qualcomm.hardware.lynx.LynxDcMotorController
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.hardware.DcMotorController

class CalcifiedModule(val lynxModule: LynxModule) : OpModeManagerNotifier.Notifications {
	private var motors = HashMap<Byte, CalcifiedMotor>()

	fun getMotor(port: Byte): CalcifiedMotor {
		// todo check if apiMotorFirst and Last are the right values here, they might be a little off or better selections could be made to ensure future proofing
		if (port !in LynxDcMotorController.apiMotorFirst..LynxDcMotorController.apiMotorLast) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxDcMotorController.apiMotorFirst}, ${LynxDcMotorController.apiMotorLast}")
		motors.putIfAbsent(port, CalcifiedMotor(this, port))
		return motors[port]!!
	}

	override fun onOpModePreInit(opMode: OpMode?) {
		TODO("Not yet implemented")
	}

	override fun onOpModePreStart(opMode: OpMode?) {
		TODO("Not yet implemented")
	}

	override fun onOpModePostStop(opMode: OpMode?) {
		TODO("Not yet implemented")
	}
}