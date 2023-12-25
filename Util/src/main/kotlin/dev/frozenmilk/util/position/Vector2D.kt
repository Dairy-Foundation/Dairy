package dev.frozenmilk.util.position

import dev.frozenmilk.util.angle.Angle
import dev.frozenmilk.util.angle.AngleRadians
import java.util.Objects
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

class Vector2D @JvmOverloads constructor(val units: DistanceUnits = DistanceUnits.MILLIMETER, val x: Double = 0.0, val y: Double = 0.0) {
	/**
	 * angle of the vector, always an [AngleRadians] under the hood
	 */
	val theta: Angle by lazy { AngleRadians(atan2(y, x)) }

	/**
	 * length of the vector, in [units]
	 */
	val magnitude: Double by lazy { hypot(x ,y) }

	/**
	 * polar constructor
	 */
	constructor(units: DistanceUnits, r: Double, t: Angle) : this(units, r, t.intoRadians())

	/**
	 * polar constructor
	 *
	 * internal handler
	 */
	private constructor(units: DistanceUnits, r: Double, t: AngleRadians) : this(units, r * cos(t.theta), r * sin(t.theta))

	/**
	 * non-mutating
	 */
	infix fun into(units: DistanceUnits) = if (this.units == units) this else Vector2D(units, this.units.into(units, x), this.units.into(units, y))

	/**
	 * non-mutating
	 */
	operator fun plus(vector2D: Vector2D) = this internalPlus vector2D.into(units)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalPlus(vector2D: Vector2D) = Vector2D(units, x + vector2D.x, y + vector2D.y)

	/**
	 * non-mutating
	 */
	operator fun minus(vector2D: Vector2D) = this internalMinus vector2D.into(units)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalMinus(vector2D: Vector2D) = Vector2D(units, x - vector2D.x, y - vector2D.y)

	/**
	 * non-mutating
	 *
	 * has no effect
	 */
	operator fun unaryPlus(): Vector2D = this

	/**
	 * non-mutating
	 *
	 * equivalent to [rotate] 180 degrees
	 *
	 * also equivalent to [times] -1.0
	 */
	operator fun unaryMinus() = this * -1.0

	/**
	 * non-mutating
	 */
	operator fun times(scalar: Double) = Vector2D(units, x * scalar, y * scalar)

	/**
	 * non-mutating
	 */
	infix fun rotate(angle: Angle) = this rotate angle.intoRadians()

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun rotate(angle: AngleRadians): Vector2D {
		val cos = cos(angle.theta)
		val sin = sin(angle.theta)
		return Vector2D(units, cos * x - sin * y, sin * x + cos * y)
	}

	/**
	 * non-mutating
	 */
	fun normalise(length: Double = 1.0) = times(length / magnitude)

	/**
	 * non-mutating
	 */
	infix fun dot(vector2D: Vector2D) = this internalDot vector2D.into(units)

	/**
	 * non-mutating
	 *
	 * internal handler
	 */
	private infix fun internalDot(vector2D: Vector2D) = x * vector2D.x + y * vector2D.y

	override fun toString(): String = "($x, $y)"

	override fun equals(other: Any?): Boolean = other is Vector2D && this internalEquals other.into(units)
	private infix fun internalEquals(other: Vector2D) = abs(x - other.x) < (1E-12 * max(abs(x), abs(other.x))) && abs(y - other.y) < (1E-12 * max(abs(y), abs(other.y)))
	override fun hashCode(): Int = Objects.hash(x, y)
}

fun millimeterVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.MILLIMETER, x, y)
fun inchVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.INCH, x, y)
fun meterVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.METER, x, y)
fun footVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.FOOT, x, y)
