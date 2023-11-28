package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.MotorControllerGroup

abstract class Controller<IN>(var motorControllerGroup: MotorControllerGroup, var errorSupplier: ErrorSupplier<IN, Double>) {
	protected abstract fun calculate(error: Double): Double
	fun update(setPoint: IN) {
		motorControllerGroup.setPowers(calculate(errorSupplier.getError(setPoint)))
	}
}