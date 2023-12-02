package dev.frozenmilk.dairy.ftclink.geometry.angle

class AngleRadians(theta: Double = 0.0) : Angle(theta) {

	@Suppress("NAME_SHADOWING")
	override fun normalise(theta: Double): Double {
		var theta = theta % (ROTATION);
		if (theta < 0.0) {
			theta += ROTATION;
		}
		return theta;
	}

	override fun plus(other: Angle): AngleRadians {
		return AngleRadians(this.theta + other.intoRadians().theta)
	}

	override fun minus(other: Angle): AngleRadians {
		return AngleRadians(this.theta - other.intoRadians().theta)
	}

	override fun unaryPlus(): AngleRadians {
		return this
	}

	override fun unaryMinus(): AngleRadians {
		return AngleRadians(-this.theta)
	}

	override fun intoRadians(): AngleRadians {
		return this
	}

	override fun intoDegrees(): AngleDegrees {
		return AngleDegrees(Math.toDegrees(theta))
	}

	/**
	 * result is in the domain [-PI, PI]
	 */
	override fun findShortestDistance(other: Angle): Double {
		val difference: Double = other.intoRadians().theta - this.theta
		if (difference > Math.PI) {
			return -ROTATION + difference
		} else if (difference < -Math.PI) {
			return ROTATION + difference
		}
		return difference
	}

	private companion object {
		private const val ROTATION = 2.0 * Math.PI
	}

	override fun toString(): String {
		return "$theta RAD"
	}

	override fun equals(other: Any?): Boolean {
		if (other !is Angle) return false
		return theta == other.intoRadians().theta
	}

	override fun hashCode(): Int {
		return javaClass.hashCode()
	}
}