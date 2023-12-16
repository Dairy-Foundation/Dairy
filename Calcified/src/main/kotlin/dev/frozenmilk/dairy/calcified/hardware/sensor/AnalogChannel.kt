package dev.frozenmilk.dairy.calcified.hardware.sensor

import dev.frozenmilk.dairy.calcified.gamepad.EnhancedBooleanSupplier
import dev.frozenmilk.dairy.calcified.gamepad.EnhancedNumberSupplier
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import java.util.function.Supplier

class AnalogInput(private val module: CalcifiedModule, private val port: Byte) : Supplier<Double> {
	private var cachedVoltage: Double? = null

	/**
	 * returns the voltage reported by the sensor in milliVolts
	 */
	override fun get(): Double {
		if (cachedVoltage == null) cachedVoltage = module.bulkData.getAnalogInput(port.toInt()).toDouble()
		return cachedVoltage!!
	}

	/**
	 * clears the cache
	 *
	 * may be automatically called, depending on settings you have
	 */
	fun clearCache() {
		cachedVoltage = null
	}

	/**
	 * an [EnhancedBooleanSupplier] version of this
	 */
	var enhanced = EnhancedNumberSupplier(this)
}

