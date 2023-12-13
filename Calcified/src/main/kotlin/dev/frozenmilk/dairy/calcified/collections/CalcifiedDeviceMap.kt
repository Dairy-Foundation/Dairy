package dev.frozenmilk.dairy.calcified.collections

import com.qualcomm.hardware.lynx.commands.core.LynxFirmwareVersionManager
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.calcified.hardware.AngleBasedRobotOrientation
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedContinuousServo
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedEncoder
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedIMU
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedServo
import dev.frozenmilk.dairy.calcified.hardware.DegreesEncoder
import dev.frozenmilk.dairy.calcified.hardware.PWMDevice
import dev.frozenmilk.dairy.calcified.hardware.RadiansEncoder
import dev.frozenmilk.dairy.calcified.hardware.TicksEncoder
import dev.frozenmilk.dairy.calcified.hardware.UnitEncoder
import org.firstinspires.ftc.robotcore.internal.system.AppUtil

abstract class CalcifiedDeviceMap<T> internal constructor(protected val module: CalcifiedModule, private val map: MutableMap<Byte, T> = mutableMapOf()) : MutableMap<Byte, T> by map

class Motors internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<CalcifiedMotor>(module) {
	fun getMotor(port: Byte): CalcifiedMotor {
		// checks to confirm that the motor port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_MOTOR_PORT}, ${LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS - 1}]")
		this.putIfAbsent(port, CalcifiedMotor(module, port))
		return this[port]!!
	}
}

class Servos internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<PWMDevice>(module) {
	fun getServo(port: Byte): CalcifiedServo {
		if (port !in LynxConstants.INITIAL_SERVO_PORT until LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_SERVO_PORT}, ${LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1}]")
		if (this.containsKey(port) && this[port] !is CalcifiedServo) {
			this[port] = CalcifiedServo(module, port)
		}
		return (this[port] as CalcifiedServo)
	}

	fun getContinuousServo(port: Byte): CalcifiedContinuousServo {
		if (port !in LynxConstants.INITIAL_SERVO_PORT until LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_SERVO_PORT}, ${LynxConstants.INITIAL_SERVO_PORT + LynxConstants.NUMBER_OF_SERVO_CHANNELS - 1}]")
		if (this.containsKey(port) && this[port] !is CalcifiedContinuousServo) {
			this[port] = CalcifiedContinuousServo(module, port)
		}
		return (this[port] as CalcifiedContinuousServo)
	}
}

class Encoders internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<CalcifiedEncoder<*>>(module) {

	/**
	 * if the port is empty, makes a new [TicksEncoder], else, overrides the encoder on the port
	 */
	fun getTicksEncoder(port: Byte): TicksEncoder {
		// this is pretty much the same as the motors, as the encoders match the motors
		// checks to confirm that the encoder port is validly in range
		if (port !in LynxConstants.INITIAL_MOTOR_PORT until LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS) throw IllegalArgumentException("$port is not in the acceptable port range [${LynxConstants.INITIAL_MOTOR_PORT}, ${LynxConstants.INITIAL_MOTOR_PORT + LynxConstants.NUMBER_OF_MOTORS - 1}]")
		this[port] = TicksEncoder(module, port)
		return (this[port] as TicksEncoder?)!!
	}

	/**
	 * This method is useful for if you have your own [UnitEncoder] overrides, for your own types, most of the time you want to use one of the other get<type>Encoder methods on this module
	 *
	 * @return Overrides the encoder on the port with a [UnitEncoder] of the supplied type, with the [ticksPerUnit] specified
	 */
	fun <T : UnitEncoder<*>> getEncoder(type: Class<out T>, port: Byte, ticksPerUnit: Double): T {
		val ticksEncoder = getTicksEncoder(port)
		this[port] = type.getDeclaredConstructor(TicksEncoder::class.java, Double::class.java).newInstance(ticksEncoder, ticksPerUnit)
		return type.cast(this[port])!!
	}

	/**
	 * overrides the encoder on the port with a RadiansEncoder, with the ticksPerRevolution specified
	 */
	fun getRadiansEncoder(port: Byte, ticksPerRevolution: Double): RadiansEncoder {
		return getEncoder(RadiansEncoder::class.java, port, ticksPerRevolution)
	}

	/**
	 * overrides the encoder on the port with a DegreesEncoder, with the ticksPerRevolution specified
	 */
	fun getDegreesEncoder(port: Byte, ticksPerRevolution: Double): DegreesEncoder {
		return getEncoder(DegreesEncoder::class.java, port, ticksPerRevolution)
	}
}

class IMUs internal constructor(module: CalcifiedModule) : CalcifiedDeviceMap<CalcifiedIMU>(module){
	fun getIMU(port: Byte, imuType: LynxModuleImuType, angleBasedRobotOrientation: AngleBasedRobotOrientation): CalcifiedIMU {
		this.putIfAbsent(port, CalcifiedIMU(imuType, LynxFirmwareVersionManager.createLynxI2cDeviceSynch(AppUtil.getDefContext(), module.lynxModule, port.toInt()), angleBasedRobotOrientation))
		return this[port]!!
	}

	fun getIMU_BHI260(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()) = this.getIMU(port, LynxModuleImuType.BHI260, angleBasedRobotOrientation)

	fun getIMU_BNO055(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()) = this.getIMU(port, LynxModuleImuType.BNO055, angleBasedRobotOrientation)
}