package dev.frozenmilk.util.angle

class AngleDegrees(theta: Double = 0.0) : Angle(theta) {
	@Suppress("NAME_SHADOWING")
	override fun normalise(theta: Double): Double {
		var theta = theta % 360.0
		if (theta < 0.0) {
			theta += 360.0
		}
		return theta
	}

	override fun plus(other: Angle): AngleDegrees {
		return AngleDegrees(this.theta + other.intoDegrees().theta)
	}

	override fun minus(other: Angle): AngleDegrees {
		return AngleDegrees(this.theta - other.intoDegrees().theta)
	}

	override fun unaryPlus(): AngleDegrees {
		return this
	}

	override fun unaryMinus(): AngleDegrees {
		return AngleDegrees(-this.theta)
	}

	override fun intoRadians(): AngleRadians {
		return AngleRadians(Math.toRadians(theta))
	}

	override fun intoDegrees(): AngleDegrees {
		return this
	}

	/**
	 * result is in the domain [-180, 180]
	 */
	override fun findShortestDistance(other: Angle): Double {
		val difference: Double = other.intoDegrees().theta - this.theta
		if (difference > 180) {
			return -360 + difference
		} else if (difference < -180) {
			return 360 + difference
		}
		return difference
	}

	override fun toString(): String {
		return "$theta DEG"
	}
}