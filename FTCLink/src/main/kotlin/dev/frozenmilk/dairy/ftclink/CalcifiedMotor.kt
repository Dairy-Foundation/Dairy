package dev.frozenmilk.dairy.ftclink

import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import kotlin.math.abs

class CalcifiedMotor {
	var direction = Direction.FORWARD
	var cachingTolerance = 0.02
	var enabled = true

	var zeroPowerBehavior = ZeroPowerBehavior.FLOAT
		set(value) {
			if (field != zeroPowerBehavior) TODO("send zero power command")

			field = value
		}

	var power = 0.0
		get() = if (enabled) field else 0.0
		set(value) {
			field = if (abs(power - value) > cachingTolerance) {
				TODO("send power command")
			} else {
				power
			}
		}

	fun enable() {
		enabled = true
	}

	fun disable() {
		enabled = false
	}

	enum class Direction(val multiplier: Byte) {
		FORWARD(1),
		REVERSE(-1)
	}
}