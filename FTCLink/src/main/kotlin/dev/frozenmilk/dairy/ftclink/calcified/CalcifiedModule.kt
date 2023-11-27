package dev.frozenmilk.dairy.ftclink.calcified

import com.qualcomm.hardware.lynx.LynxDcMotorController
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedEncoder
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor

class CalcifiedModule(val lynxModule: LynxModule) {
	private var motors = HashMap<Byte, CalcifiedMotor>().toMutableMap()
	private var encoders = HashMap<Byte, CalcifiedEncoder>().toMutableMap()
	private var deviceMap: MutableMap<Class<*>, MutableMap<Byte, out Any>> = mapOf(
			CalcifiedMotor::class.java to motors,
			CalcifiedEncoder::class.java to encoders
	).toMutableMap()

	fun <T> unsafeGet(type: Class<out T>, port: Byte): T {
		val resultMap = deviceMap[type]
				?: throw IllegalArgumentException("no mappings of type ${type.simpleName} in this module's device mapping")
		val result = resultMap[port]
				?: throw IllegalArgumentException("no device of type ${type.simpleName} found at port $port")
		return type.cast(result)
				?: throw IllegalArgumentException("failed to cast device to type ${type.simpleName}")
	}

	fun getMotor(port: Byte): CalcifiedMotor {
		// checks to confirm that the motor port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxDcMotorController.apiMotorFirst}, ${LynxDcMotorController.apiMotorLast}")
		motors.putIfAbsent(port, CalcifiedMotor(this, port))
		return motors[port]!!
	}

	fun getEncoder(port: Byte): CalcifiedEncoder {
		// this is pretty much the same as the motors, as the encoders match the motors
		// checks to confirm that the encoder port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxDcMotorController.apiMotorFirst}, ${LynxDcMotorController.apiMotorLast}")
		encoders.putIfAbsent(port, CalcifiedEncoder(this, port))
		return encoders[port]!!
	}

	lateinit var bulkData: LynxGetBulkInputDataResponse
		private set

	init {
		refreshBulkCache()
	}

	fun refreshBulkCache() {
		val command = LynxGetBulkInputDataCommand(lynxModule)
		bulkData = command.sendReceive();
	}
}
