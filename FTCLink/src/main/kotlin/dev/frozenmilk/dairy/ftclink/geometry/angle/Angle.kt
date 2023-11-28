package dev.frozenmilk.dairy.ftclink.geometry.angle

abstract class Angle(theta: Double = 0.0) {
	var theta: Double = this.absolute(theta)
		protected set(value) {
			field = absolute(value)
		}
		get() = absolute(field)

	abstract fun absolute(theta: Double): Double

	abstract operator fun plus(other: Angle): Angle
	abstract operator fun plus(other: Double): Angle
	abstract operator fun minus(other: Angle): Angle
	abstract operator fun minus(other: Double): Angle
	abstract fun intoRadians(): AngleRadians
	abstract fun intoDegrees(): AngleDegrees

	/**
	 * returns the shortest distance from this to other, in the unit type of this
	 */
	abstract fun findShortestDistance(other: Angle): Double
}