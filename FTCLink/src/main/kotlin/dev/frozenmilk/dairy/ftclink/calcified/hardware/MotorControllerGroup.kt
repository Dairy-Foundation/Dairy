package dev.frozenmilk.dairy.ftclink.calcified.hardware

import java.util.function.DoubleFunction

class MotorControllerGroup(vararg motors: CalcifiedMotor) {
	val motors = motors.toMutableList()
	var controller: DoubleFunction<Double>? = null

	fun setPowers(power: Double) {
		motors.forEach { it.power = power }
	}

	fun calculateController(target: Double) {
		setPowers(controller?.apply(target)
				?: throw IllegalArgumentException("Attempted to run MotorControllerGroup.calculateController when controller is null"))
	}
}
