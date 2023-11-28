package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup

class PController(motorControllerGroup: MotorControllerGroup, errorSupplier: ErrorSupplier<Any>, var kP: Double) : Controller<Any>(motorControllerGroup, errorSupplier) {
	override fun calculate(error: Double): Double {
		return error * kP
	}
}