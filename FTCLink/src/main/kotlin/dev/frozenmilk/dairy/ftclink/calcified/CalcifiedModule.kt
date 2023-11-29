package dev.frozenmilk.dairy.ftclink.calcified

import com.qualcomm.hardware.lynx.LynxDcMotorController
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.ftclink.calcified.collections.Encoders
import dev.frozenmilk.dairy.ftclink.calcified.collections.Motors
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedEncoder
import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor
import dev.frozenmilk.dairy.ftclink.calcified.hardware.RadiansEncoder
import dev.frozenmilk.dairy.ftclink.calcified.hardware.TicksEncoder
import dev.frozenmilk.dairy.ftclink.calcified.hardware.UnitEncoder

class CalcifiedModule(val lynxModule: LynxModule) {
	val motors = Motors(this)
	val encoders = Encoders(this)
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

	var cachedTime: Double = System.nanoTime() / 1E9
		private set
	var previousCachedTime: Double = cachedTime
	lateinit var bulkData: LynxGetBulkInputDataResponse
		private set

	init {
		refreshBulkCache()
	}

	fun refreshBulkCache() {
		// update cached time first
		previousCachedTime = cachedTime
		cachedTime = System.nanoTime() / 1E9

		// encoders rely on the current bulk cache info, we ensure that the encoders get to look at it before we clear it
		encoders.forEach { (_, encoder) -> encoder.clearCache() }

		// finally, update the bulk cache
		val command = LynxGetBulkInputDataCommand(lynxModule)
		bulkData = command.sendReceive();
	}
}


