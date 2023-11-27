package dev.frozenmilk.dairy.ftclink.calcified.hardware

import dev.frozenmilk.dairy.ftclink.calcified.CalcifiedModule
import java.util.function.Supplier

class CalcifiedEncoder internal constructor(private val module: CalcifiedModule, private val port: Byte) : Supplier<Double> {
	var direction = Direction.FORWARD

	fun getTicks(): Int {
		return module.bulkData.getEncoder(port.toInt()) * direction.multiplier
	}

	override fun get(): Double = getTicks().toDouble() // todo allow this to be switched to supply the value in some other unit type
}