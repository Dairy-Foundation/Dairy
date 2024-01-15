package dev.frozenmilk.util.units

import java.util.function.Supplier
import kotlin.math.abs

/**
 * common value is millimeters
 */
interface DistanceUnit : Unit<DistanceUnit>
enum class DistanceUnits(override val toCommonRatio: Double) : DistanceUnit {
	METER(1000.0),
	MILLIMETER(1.0),
	INCH(25.4),
	FOOT(304.8),
}

class Distance(val distanceUnit: DistanceUnit = DistanceUnits.MILLIMETER, value: Double = 0.0) : ReifiedUnit<DistanceUnit, Distance>(value) {
	override fun into(unit: DistanceUnit) = Distance(unit, distanceUnit.into(unit, value))
	override fun plus(reifiedUnit: ReifiedUnit<DistanceUnit, Distance>) = Distance(distanceUnit, value + reifiedUnit.into(distanceUnit).value)
	override fun minus(reifiedUnit: ReifiedUnit<DistanceUnit, Distance>) = Distance(distanceUnit, value - reifiedUnit.into(distanceUnit).value)
	override fun unaryPlus() = this
	override fun unaryMinus() = Distance(distanceUnit, -value)
	override fun times(multiplier: Double) = Distance(distanceUnit, value * multiplier)
	override fun times(reifiedUnit: ReifiedUnit<DistanceUnit, Distance>) = Distance(distanceUnit, value * reifiedUnit.into(distanceUnit).value)
	override fun div(divisor: Double) = Distance(distanceUnit, value / divisor)
	override fun div(reifiedUnit: ReifiedUnit<DistanceUnit, Distance>) = Distance(distanceUnit, value / reifiedUnit.into(distanceUnit).value)
	override fun compareTo(other: ReifiedUnit<DistanceUnit, Distance>): Int = value.compareTo(other.into(distanceUnit).value)
	override fun toString() = "$value $distanceUnit"
	override fun equals(other: Any?): Boolean = other is Distance && abs((this - other).value) < 1e-12
	override fun hashCode(): Int = intoMillimeters().value.hashCode()

	// quick intos
	fun intoMillimeters() = into(DistanceUnits.MILLIMETER)
	fun intoInches() = into(DistanceUnits.INCH)
	fun intoFeet() = into(DistanceUnits.FOOT)
	fun intoMeters() = into(DistanceUnits.METER)
}

fun Supplier<out Distance>.into(unit: DistanceUnit) = Supplier<Distance> { get().into(unit) }
fun Supplier<out Distance>.intoMillimeters() = Supplier<Distance> { get().intoMillimeters() }
fun Supplier<out Distance>.intoInches() = Supplier<Distance> { get().intoInches() }
fun Supplier<out Distance>.intoFeet() = Supplier<Distance> { get().intoFeet() }
fun Supplier<out Distance>.intoMeters() = Supplier<Distance> { get().intoMeters() }
