package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import dev.frozenmilk.dairy.calcified.collections.AnalogInputs
import dev.frozenmilk.dairy.calcified.collections.DigitalChannels
import dev.frozenmilk.dairy.calcified.collections.Encoders
import dev.frozenmilk.dairy.calcified.collections.I2CDevices
import dev.frozenmilk.dairy.calcified.collections.Motors
import dev.frozenmilk.dairy.calcified.collections.PWMDevices
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedEncoder
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.motor.DegreesEncoder
import dev.frozenmilk.dairy.calcified.hardware.motor.RadiansEncoder
import dev.frozenmilk.dairy.calcified.hardware.motor.TicksEncoder
import dev.frozenmilk.dairy.calcified.hardware.sensor.AnalogInput
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMU
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalInput
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalOutput
import dev.frozenmilk.dairy.calcified.hardware.servo.CalcifiedContinuousServo
import dev.frozenmilk.dairy.calcified.hardware.servo.CalcifiedServo
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.orientation.AngleBasedRobotOrientation

class CalcifiedModule(val lynxModule: LynxModule) {
	val motors = Motors(this)
	val encoders = Encoders(this)
	val PWMDevices = PWMDevices(this)
	val i2cDevices = I2CDevices(this)
	val digitalChannels = DigitalChannels(this)
	val analogInputs = AnalogInputs(this)
	var deviceMap: MutableMap<Class<*>, MutableMap<Byte, out Any>> = mutableMapOf(
			CalcifiedMotor::class.java to motors,
			CalcifiedEncoder::class.java to encoders,
			CalcifiedServo::class.java to PWMDevices,
			CalcifiedIMU::class.java to i2cDevices,
			DigitalInput::class.java to digitalChannels,
			DigitalOutput::class.java to digitalChannels,
			AnalogInput::class.java to analogInputs,
	)
		private set

	fun <T> unsafeGet(type: Class<out T>, port: Byte): T {
		val resultMap = deviceMap[type]
				?: throw IllegalArgumentException("no mappings of type ${type.simpleName} in this module's device mapping")
		val result = resultMap[port]
				?: throw IllegalArgumentException("no device of type ${type.simpleName} found at port $port")
		return type.cast(result)
				?: throw IllegalArgumentException("failed to cast device to type ${type.simpleName}")
	}

	var cachedTime: Double = System.nanoTime() / 1E9
		private set
	var previousCachedTime: Double = cachedTime
	var bulkData: LynxGetBulkInputDataResponse by LateInitCell()

	init {
		refreshBulkCache()
	}

	fun refreshBulkCache() {
		// update cached time first
		previousCachedTime = cachedTime
		cachedTime = System.nanoTime() / 1E9

		// encoders rely on the current bulk cache info, we ensure that the encoders get to look at it before we clear it
		encoders.forEach { (_, encoder) -> encoder.clearCache() }
		i2cDevices.forEach { (_, imu) -> (imu as? CalcifiedIMU)?.let { imu.clearCache() } }
		digitalChannels
				.filter { (_, digitalChannel) -> digitalChannel is DigitalInput }
				.forEach { (_, input) -> (input as DigitalInput).clearCache() }

		// finally, update the bulk cache
		bulkData = LynxGetBulkInputDataCommand(lynxModule).sendReceive();
	}

	//
	// Remaps to the device maps
	//

	fun getMotor(port: Byte): CalcifiedMotor {
		return motors.getMotor(port)
	}

	fun getTicksEncoder(port: Byte): TicksEncoder {
		return encoders.getTicksEncoder(port)
	}

	fun getRadiansEncoder(port: Byte, ticksPerRevolution: Double): RadiansEncoder {
		return encoders.getRadiansEncoder(port, ticksPerRevolution)
	}

	fun getDegreesEncoder(port: Byte, ticksPerRevolution: Double): DegreesEncoder {
		return encoders.getDegreesEncoder(port, ticksPerRevolution)
	}

	fun getServo(port: Byte): CalcifiedServo {
		return PWMDevices.getServo(port)
	}

	fun getContinuousServo(port: Byte): CalcifiedContinuousServo {
		return PWMDevices.getContinuousServo(port)
	}

	@JvmOverloads
	fun getIMU_BHI260(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU_BHI260(port, angleBasedRobotOrientation)
	}

	@JvmOverloads
	fun getIMU_BNO055(port: Byte, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU_BNO055(port, angleBasedRobotOrientation)
	}

	@JvmOverloads
	fun getIMU(port: Byte, lynxModuleImuType: LynxModuleImuType, angleBasedRobotOrientation: AngleBasedRobotOrientation = AngleBasedRobotOrientation()): CalcifiedIMU {
		return i2cDevices.getIMU(port, lynxModuleImuType, angleBasedRobotOrientation)
	}

	fun getDigitalInput(port: Byte): DigitalInput {
		return digitalChannels.getInput(port)
	}

	fun getDigitalOutput(port: Byte): DigitalOutput {
		return digitalChannels.getOutput(port)
	}

	fun getAnalogInput(port: Byte): AnalogInput {
		return analogInputs.getInput(port)
	}
}
