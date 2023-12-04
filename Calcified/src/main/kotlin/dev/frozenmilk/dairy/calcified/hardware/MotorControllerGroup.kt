package dev.frozenmilk.dairy.calcified.hardware

open class MotorControllerGroup(motors: Collection<CalcifiedMotor>) : Collection<CalcifiedMotor> by motors {
	val motors = motors.toMutableList()

	fun setPowers(power: Double) {
		motors.forEach { it.power = power }
	}
}
