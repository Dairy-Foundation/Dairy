package dev.frozenmilk.dairy.ftclink.calcified.controller

import java.util.function.DoubleFunction
import java.util.function.Supplier

class PController(var kP: Double, var positionSupplier: Supplier<Double>) : DoubleFunction<Double> {
	override fun apply(p0: Double): Double {
		return (positionSupplier.get() - p0) * kP
	}
}