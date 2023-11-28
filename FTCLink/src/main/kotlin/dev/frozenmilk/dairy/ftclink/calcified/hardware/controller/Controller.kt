package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup

abstract class Controller<T>(var motorControllerGroup: MotorControllerGroup, var errorSupplier: ErrorSupplier<T>) {
	protected abstract fun calculate(error: Double): Double
	fun update(setPoint: T) {
		motorControllerGroup.setPowers(calculate(errorSupplier.getError(setPoint)))
	}
}