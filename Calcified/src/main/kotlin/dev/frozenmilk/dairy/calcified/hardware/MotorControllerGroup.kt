package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
import kotlin.math.abs

class MotorControllerGroup(motors: Collection<SimpleMotor>) : Collection<SimpleMotor> by motors, SimpleMotor {
	constructor(vararg motors: SimpleMotor) : this(motors.toList())
	override var direction: Direction = Direction.FORWARD
		set(value) {
			if (field != value) {
				forEach {
					it.direction = when (it.direction) {
						Direction.FORWARD -> Direction.REVERSE
						Direction.REVERSE -> Direction.FORWARD
					}
				}
				field = value
			}
		}
	override var cachingTolerance: Double = 0.005
	override var enabled: Boolean = true
	override var power: Double = 0.0
		get() = if (enabled) field * direction.multiplier else 0.0
		set(value) {
			if (!enabled) return
			val correctedValue = value.coerceIn(-1.0, 1.0) * direction.multiplier
			if (abs(field - correctedValue) >= cachingTolerance || (correctedValue >= 1.0 && field != 1.0) || (correctedValue <= -1.0 && field != -1.0)) {
				forEach { it.power = correctedValue }
				field = correctedValue
			}
		}
}