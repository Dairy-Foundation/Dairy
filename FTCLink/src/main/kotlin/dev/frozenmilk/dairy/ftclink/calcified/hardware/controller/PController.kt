package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup

class PController<T>(motorControllerGroup: MotorControllerGroup, errorSupplier: ErrorSupplier<T>, var kP: Double) : Controller<T>(motorControllerGroup, errorSupplier) {
	override fun calculate(error: Double): Double {
		return error * kP
	}
}