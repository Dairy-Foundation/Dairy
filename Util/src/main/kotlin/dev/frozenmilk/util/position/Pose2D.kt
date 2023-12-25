package dev.frozenmilk.util.position

import dev.frozenmilk.util.angle.Angle
import dev.frozenmilk.util.angle.AngleRadians
import java.util.Objects

class Pose2D @JvmOverloads constructor(val units: DistanceUnits = DistanceUnits.MILLIMETER, vector2D: Vector2D = Vector2D(units), val heading: Angle = AngleRadians()) {
	val vector2D = vector2D.into(units)
	constructor(units: DistanceUnits, x: Double, y: Double, heading: Angle) : this(units, Vector2D(units, x, y).into(units), heading)

	/**
	 * non-mutating
	 */
	infix fun into(units: DistanceUnits) = if (this.units == units) this else Pose2D(units, vector2D into units, heading)

	/**
	 * non-mutating
	 */
	operator fun plus(pose2D: Pose2D) = this internalPlus pose2D.into(units)

	/**
	 * non-mutating
	 */
	operator fun plus(vector2D: Vector2D) = this internalPlus vector2D.into(units)

	/**
	 * non-mutating
	 */
	operator fun plus(heading: Angle) = Pose2D(units, vector2D, this.heading + heading)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalPlus(vector2D: Vector2D) = Pose2D(units, this.vector2D + vector2D, heading)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalPlus(pose2D: Pose2D) = Pose2D(units, vector2D + pose2D.vector2D, heading + pose2D.heading)

	/**
	 * non-mutating
	 */
	operator fun minus(pose2D: Pose2D) = this internalMinus pose2D.into(units)

	/**
	 * non-mutating
	 */
	operator fun minus(vector2D: Vector2D) = this internalMinus vector2D.into(units)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalMinus(vector2D: Vector2D) = Pose2D(units, this.vector2D - vector2D, heading)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalMinus(pose2D: Pose2D) = Pose2D(units, vector2D - pose2D.vector2D, heading - pose2D.heading)

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
	operator fun unaryMinus() = Pose2D(units, -vector2D, -heading)

	override fun toString() = "$vector2D, $heading"

	override fun equals(other: Any?) = other is Pose2D && vector2D == other.vector2D && heading == other.heading

	override fun hashCode() = Objects.hash(vector2D, heading)
}

fun millimeterPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.MILLIMETER, x, y, heading)
fun inchPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.INCH, x, y, heading)
fun meterPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.METER, x, y, heading)
fun footPose(x: Double = 0.0, y: Double = 0.0, heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.FOOT, x, y, heading)

fun millimeterPose(vector2D: Vector2D = millimeterVector(), heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.MILLIMETER, vector2D.into(DistanceUnits.MILLIMETER), heading)
fun inchPose(vector2D: Vector2D = inchVector(), heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.INCH, vector2D.into(DistanceUnits.INCH), heading)
fun meterPose(vector2D: Vector2D = meterVector(), heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.MILLIMETER, vector2D.into(DistanceUnits.MILLIMETER), heading)
fun footPose(vector2D: Vector2D = footVector(), heading: Angle = AngleRadians()) = Pose2D(DistanceUnits.MILLIMETER, vector2D.into(DistanceUnits.MILLIMETER), heading)
