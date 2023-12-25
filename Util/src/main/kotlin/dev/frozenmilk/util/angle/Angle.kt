package dev.frozenmilk.util.angle

import kotlin.math.abs

/**
 * immutably represents a normalised angle in the domain of [0, 1] rotations.
 *
 * subclasses represent angles in specific mathematical units
 *
 * @see [AngleDegrees]
 * @see [AngleRadians]
 */
abstract class Angle(theta: Double = 0.0) {
	val theta: Double = this.normalise(theta)
		get() = normalise(field)

	protected abstract fun normalise(theta: Double): Double

	/**
	 * non-mutating
	 */
	abstract operator fun plus(other: Angle): Angle

	/**
	 * non-mutating
	 */
	abstract operator fun minus(other: Angle): Angle

	/**
	 * non-mutating, does nothing
	 */
	abstract operator fun unaryPlus(): Angle

	/**
	 * non-mutating, equal to the negative rotation of this
	 */
	abstract operator fun unaryMinus(): Angle

	/**
	 * non-mutating
	 */
	abstract fun intoRadians(): AngleRadians

	/**
	 * non-mutating
	 */
	abstract fun intoDegrees(): AngleDegrees

	/**
	 * returns the shortest distance from this to other, in the unit type of this
	 */
	abstract fun findShortestDistance(other: Angle): Double

	override fun equals(other: Any?): Boolean = other is Angle && this internalEquals other
	private infix fun internalEquals(other: Angle) = abs(this.findShortestDistance(other)) < 1E-12

	override fun hashCode() = this.intoRadians().theta.hashCode()
}