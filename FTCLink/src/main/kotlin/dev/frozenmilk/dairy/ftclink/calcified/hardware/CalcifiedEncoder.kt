package dev.frozenmilk.dairy.ftclink.calcified.hardware

import dev.frozenmilk.dairy.ftclink.calcified.CalcifiedModule
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.ErrorSupplier

class CalcifiedEncoder internal constructor(private val module: CalcifiedModule, private val port: Byte) : ErrorSupplier<Double> {
	var direction = Direction.FORWARD

	fun getTicks(): Int {
		return module.bulkData.getEncoder(port.toInt()) * direction.multiplier
	}

	/**
	 * returns error in ticks, consider wrapping this encoder in an EncoderConverter to use error with some other, more predictable unit
	 */
	override fun getError(target: Double): Double {
		return target - getTicks()
	}
}