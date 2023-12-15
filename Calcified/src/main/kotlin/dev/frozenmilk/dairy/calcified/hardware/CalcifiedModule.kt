package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse
import dev.frozenmilk.dairy.calcified.collections.DigitalChannels
import dev.frozenmilk.dairy.calcified.collections.Encoders
import dev.frozenmilk.dairy.calcified.collections.IMUs
import dev.frozenmilk.dairy.calcified.collections.Motors
import dev.frozenmilk.dairy.calcified.collections.PWMDevices
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedEncoder
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMU
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalInput
import dev.frozenmilk.dairy.calcified.hardware.servo.CalcifiedServo
import dev.frozenmilk.util.cell.LateInitCell

class CalcifiedModule(val lynxModule: LynxModule) {
	val motors = Motors(this)
	val encoders = Encoders(this)
	val PWMDevices = PWMDevices(this)
	val imus = IMUs(this)
	val digitalChannels = DigitalChannels(this)
	private var deviceMap: MutableMap<Class<*>, MutableMap<Byte, out Any>> = mapOf(
			CalcifiedMotor::class.java to motors,
			CalcifiedEncoder::class.java to encoders,
			CalcifiedServo::class.java to PWMDevices,
			CalcifiedIMU::class.java to imus,
	).toMutableMap()

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
		imus.forEach { (_, imu) -> imu.clearCache() }
		digitalChannels
				.filter { (_, digitalChannel) -> digitalChannel is DigitalInput }
				.forEach { (_, input) -> (input as DigitalInput).clearCache() }

		// finally, update the bulk cache
		val command = LynxGetBulkInputDataCommand(lynxModule)
		bulkData = command.sendReceive();
	}
}


