package dev.frozenmilk.util.units.orientation

import dev.frozenmilk.util.units.Angle

/**
 * see [rotations using the right hand rule](https://en.wikipedia.org/wiki/Right-hand_rule#Rotations)
 *
 * @property xRot the rotation of the robot about the positive x-axis of the field
 * @property yRot the rotation of the robot about the positive y-axis of the field
 * @property zRot the rotation of the robot about the positive z-axis of the field
 */
class AngleBasedRobotOrientation @JvmOverloads constructor(val xRot: Angle = Angle(), val yRot: Angle = Angle(), val zRot: Angle = Angle()) {
	enum class Axis {
		X,
		Y,
		Z
	}

	/**
	 * a more programmatic way of getting an angle
	 */
	fun getAngle(axis: Axis): Angle {
		return when (axis) {
			Axis.X -> xRot
			Axis.Y -> yRot
			Axis.Z -> zRot
		}
	}

	/**
	 * used by the helper
	 */
	private constructor(o: AngleBasedRobotOrientation) : this(o.xRot, o.yRot, o.zRot)

	/**
	 * non-mutating
	 */
	operator fun plus(other: AngleBasedRobotOrientation): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(this.xRot + other.xRot, this.yRot + other.yRot, this.zRot + other.zRot)
	}

	/**
	 * non-mutating
	 */
	operator fun minus(other: AngleBasedRobotOrientation): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(this.xRot - other.xRot, this.yRot - other.yRot, this.zRot - other.zRot)
	}

	/**
	 * non-mutating, has no effect
	 */
	operator fun unaryPlus(): AngleBasedRobotOrientation {
		return this
	}

	/**
	 * non-mutating, equal to the inverse rotation about all axis
	 */
	operator fun unaryMinus(): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(-this.xRot, -this.yRot, -this.zRot)
	}

	private companion object {
	}
}