package dev.frozenmilk.util.position

enum class DistanceUnits (val toMillimetersRatio: Double) {
	METER(1000.0),
	MILLIMETER(1.0),
	INCH(25.4),
	FOOT(304.8);
	fun toMillimeters(value: Double): Double = value * toMillimetersRatio
	fun fromMillimeters(value: Double): Double = value / toMillimetersRatio
	fun into(units: DistanceUnits, value: Double): Double = if (units == this) value else units.fromMillimeters(toMillimeters(value))
}