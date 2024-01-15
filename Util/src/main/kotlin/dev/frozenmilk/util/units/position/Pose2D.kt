package dev.frozenmilk.util.units.position

import dev.frozenmilk.util.units.DistanceUnit
import dev.frozenmilk.util.units.DistanceUnits
import dev.frozenmilk.util.units.Angle
import java.util.Objects

class Pose2D @JvmOverloads constructor(val vector2D: Vector2D = Vector2D(), val heading: Angle = Angle()) {
	/**
	 * non-mutating
	 */
	infix fun into(units: DistanceUnit) = Pose2D(vector2D into units, heading)

	/**
	 * non-mutating
	 */
	operator fun plus(pose2D: Pose2D) = Pose2D(vector2D + pose2D.vector2D, heading + pose2D.heading)

	/**
	 * non-mutating
	 */
	operator fun plus(vector2D: Vector2D) = Pose2D(this.vector2D + vector2D, heading)

	/**
	 * non-mutating
	 */
	operator fun plus(heading: Angle) = Pose2D(vector2D, this.heading + heading)

	/**
	 * non-mutating
	 */
	operator fun minus(pose2D: Pose2D) = Pose2D(this.vector2D - pose2D.vector2D, this.heading - pose2D.heading)

	/**
	 * non-mutating
	 */
	operator fun minus(vector2D: Vector2D) = Pose2D(this.vector2D - vector2D, heading)

	/**
	 * non-mutating
	 *
	 * has no effect
	 */
	operator fun unaryPlus() = this

	/**
	 * non-mutating
	 *
	 * equivalent of rotating the vector 180 degrees, and rotating the heading 180 degrees
	 */
	operator fun unaryMinus() = Pose2D(-vector2D, -heading)

	override fun toString() = "$vector2D, $heading"

	override fun equals(other: Any?) = other is Pose2D && vector2D == other.vector2D && heading == other.heading

	override fun hashCode() = Objects.hash(vector2D, heading)
}

fun millimeterPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = Angle()) = Pose2D(millimeterVector(x, y), heading)
fun inchPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = Angle()) = Pose2D(inchVector(x, y), heading)
fun meterPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = Angle()) = Pose2D(meterVector(x, y), heading)
fun footPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = Angle()) = Pose2D(footVector(x, y), heading)

fun millimeterPose(vector2D: Vector2D = millimeterVector(), heading: Angle = Angle()) = Pose2D(vector2D.into(DistanceUnits.MILLIMETER), heading)
fun inchPose(vector2D: Vector2D = inchVector(), heading: Angle = Angle()) = Pose2D(vector2D.into(DistanceUnits.INCH), heading)
fun meterPose(vector2D: Vector2D = meterVector(), heading: Angle = Angle()) = Pose2D(vector2D.into(DistanceUnits.MILLIMETER), heading)
fun footPose(vector2D: Vector2D = footVector(), heading: Angle = Angle()) = Pose2D(vector2D.into(DistanceUnits.MILLIMETER), heading)
