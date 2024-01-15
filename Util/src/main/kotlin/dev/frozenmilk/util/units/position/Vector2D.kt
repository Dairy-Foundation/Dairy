package dev.frozenmilk.util.units.position

import dev.frozenmilk.util.units.DistanceUnit
import dev.frozenmilk.util.units.DistanceUnits
import dev.frozenmilk.util.units.Distance
import dev.frozenmilk.util.units.Angle
import dev.frozenmilk.util.units.AngleUnits
import java.util.Objects
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class Vector2D @JvmOverloads constructor(val x: Distance = Distance(DistanceUnits.MILLIMETER, 0.0), val y: Distance = Distance(DistanceUnits.MILLIMETER, 0.0)) {
	/**
	 * angle of the vector, always a [Angle] of type [AngleUnits.RADIAN] under the hood
	 */
	val theta: Angle by lazy { Angle(AngleUnits.RADIAN, atan2(y.intoMillimeters().value, x.intoMillimeters().value)) }

	/**
	 * length of the vector, always a [Distance] of type [DistanceUnits.MILLIMETER] under the hood
	 */
	val magnitude: Distance by lazy { Distance(DistanceUnits.MILLIMETER, hypot(x.intoMillimeters().value, y.intoMillimeters().value)) }
	constructor(distanceUnit: DistanceUnit = DistanceUnits.MILLIMETER, x: Double = 0.0, y: Double = 0.0) : this(Distance(distanceUnit, x), Distance(distanceUnit, y))

	/**
	 * polar constructor
	 *
	 * internal handler
	 */
	private constructor(units: DistanceUnit, r: Double, t: Angle) : this(units, r * cos(t.intoRadians().value), r * sin(t.intoRadians().value))

	/**
	 * non-mutating
	 */
	fun into(xUnit: DistanceUnit, yUnit: DistanceUnit) = Vector2D(this.x.into(xUnit), this.y.into(yUnit))

	/**
	 * non-mutating
	 */
	infix fun into(unit: DistanceUnit) = into(unit, unit)

	/**
	 * non-mutating
	 */
	operator fun plus(vector2D: Vector2D) = Vector2D(x + vector2D.x, y + vector2D.y)

	/**
	 * non-mutating
	 */
	operator fun minus(vector2D: Vector2D) = Vector2D(x - vector2D.x, y - vector2D.y)

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
	operator fun times(scalar: Double) = Vector2D(x * scalar, y * scalar)

	/**
	 * non-mutating
	 */
	infix fun rotate(angle: Angle): Vector2D {
		val angle = angle.into(AngleUnits.RADIAN)
		val cos = cos(angle.value)
		val sin = sin(angle.value)
		return Vector2D(x * cos - y * sin, x * sin + y * cos)
	}

	/**
	 * non-mutating
	 */
	fun normalise(length: Distance = Distance(DistanceUnits.MILLIMETER, 1.0)) = times((length / magnitude).value)

	/**
	 * non-mutating
	 *
	 * The resulting [Distance] can be transformed using [ReifiedLinearUnit.into] in order to have the results behave as though all the inputs were of that unit type
	 */
	infix fun dot(vector2D: Vector2D) = x * vector2D.x + y * vector2D.y

	override fun toString(): String = "($x, $y)"
	override fun equals(other: Any?): Boolean = other is Vector2D && this.x == other.x && this.y == other.y
	override fun hashCode(): Int = Objects.hash(x, y)
}

fun millimeterVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.MILLIMETER, x, y)
fun inchVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.INCH, x, y)
fun meterVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.METER, x, y)
fun footVector(x: Double = 0.0, y: Double = 0.0) = Vector2D(DistanceUnits.FOOT, x, y)
