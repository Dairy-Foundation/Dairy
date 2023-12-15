package dev.frozenmilk.util.orientation

import dev.frozenmilk.util.angle.Angle
import dev.frozenmilk.util.angle.AngleRadians

/**
 * see [rotations using the right hand rule](https://en.wikipedia.org/wiki/Right-hand_rule#Rotations)
 *
 * @property xRot the rotation of the robot about the positive x-axis of the field
 * @property yRot the rotation of the robot about the positive y-axis of the field
 * @property zRot the rotation of the robot about the positive z-axis of the field
 */
class AngleBasedRobotOrientation(val xRot: Angle = AngleRadians(), val yRot: Angle = AngleRadians(), val zRot: Angle = AngleRadians()) {
//	constructor(orientation: Orientation) : this(fromOrientation(orientation))
//
//	constructor(quaternion: Quaternion) : this(fromQuaternion(quaternion))

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