package dev.frozenmilk.dairy.ftclink.calcified.hardware

import java.util.function.DoubleFunction

class MotorControllerGroup(vararg motors: CalcifiedMotor) {
	val motors = motors.toMutableList()

	fun setPowers(power: Double) {
		motors.forEach { it.power = power }
	}
}
