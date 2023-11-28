package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup

class PController<IN>(motorControllerGroup: MotorControllerGroup, errorSupplier: ErrorSupplier<IN, Double>, var kP: Double) : Controller<IN>(motorControllerGroup, errorSupplier) {
	override fun calculate(error: Double): Double {
		return error * kP
	}
}