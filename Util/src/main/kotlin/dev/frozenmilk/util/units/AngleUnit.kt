package dev.frozenmilk.util.units

import java.util.function.Supplier
import kotlin.math.abs

/**
 * common value is radians
 */
interface AngleUnit : Unit<AngleUnit> {
	val wrapAt: Double
	override fun into(unit: AngleUnit, value: Double) = absolute(super.into(unit, value))
	fun absolute(value: Double) = (value % wrapAt).run {
		if (this < 0) this + wrapAt
		else this
	}
}

enum class AngleUnits(override val toCommonRatio: Double, override val wrapAt: Double) : AngleUnit {
	RADIAN(1.0, Math.PI * 2),
	DEGREE(Math.PI / 180.0, 360.0),
}

class Angle(val angleUnit: AngleUnit = AngleUnits.RADIAN, value: Double = 0.0) : ReifiedUnit<AngleUnit, Angle>(angleUnit.absolute(value)) {
	override fun into(unit: AngleUnit) = Angle(unit, angleUnit.into(unit, value))
	override fun plus(reifiedUnit: ReifiedUnit<AngleUnit, Angle>) = Angle(angleUnit, value + reifiedUnit.into(angleUnit).value)
	override fun minus(reifiedUnit: ReifiedUnit<AngleUnit, Angle>) = Angle(angleUnit, value - reifiedUnit.into(angleUnit).value)
	override fun unaryPlus() = this
	override fun unaryMinus() = Angle(angleUnit, -value)
	override fun times(multiplier: Double) = Angle(angleUnit, value * multiplier)
	override fun times(reifiedUnit: ReifiedUnit<AngleUnit, Angle>) = Angle(angleUnit, value) * reifiedUnit.into(angleUnit).value
	override fun div(divisor: Double) = Angle(angleUnit, value / divisor)
	override fun div(reifiedUnit: ReifiedUnit<AngleUnit, Angle>) = Angle(angleUnit, value) / reifiedUnit.into(angleUnit).value
	override fun toString() = "$value $angleUnit"
	override fun equals(other: Any?) = other is Angle && abs((this - other).value) < 1e-12
	override fun hashCode(): Int = into(AngleUnits.RADIAN).value.hashCode()
	override fun compareTo(other: ReifiedUnit<AngleUnit, Angle>) = value.compareTo(other.into(angleUnit).value)
	fun findShortestDistance(reifiedUnit: ReifiedUnit<AngleUnit, Angle>): Double {
		val difference: Double = reifiedUnit.into(angleUnit).value - this.value
		if (difference > (angleUnit.wrapAt / 2.0)) {
			return -angleUnit.wrapAt + difference
		} else if (difference < -(angleUnit.wrapAt / 2.0)) {
			return angleUnit.wrapAt + difference
		}
		return difference
	}

	// quick intos
	fun intoDegrees() = into(AngleUnits.DEGREE)
	fun intoRadians() = into(AngleUnits.RADIAN)
}

fun Supplier<out Angle>.into(unit: AngleUnit) = Supplier<Angle> { get().into(unit) }
fun Supplier<out Angle>.intoRadians() = Supplier<Angle> { get().intoRadians() }
fun Supplier<out Angle>.intoDegrees() = Supplier<Angle> { get().intoDegrees() }
