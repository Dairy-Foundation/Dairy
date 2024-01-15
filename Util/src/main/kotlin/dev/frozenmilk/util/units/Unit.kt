package dev.frozenmilk.util.units

interface Unit<U: Unit<U>> {
	val toCommonRatio: Double
	fun into(unit: U, value: Double): Double = if (unit == this) value else unit.fromCommonUnit(toCommonUnit(value))
	fun toCommonUnit(value: Double): Double = value * toCommonRatio
	fun fromCommonUnit(value: Double): Double = value / toCommonRatio
}

abstract class ReifiedUnit<U: Unit<U>, RU: ReifiedUnit<U, RU>>(val value: Double) : Number(), Comparable<ReifiedUnit<U, RU>> {
	/**
	 * non-mutating
	 */
	abstract fun into(unit: U): RU
	/**
	 * non-mutating
	 */
	abstract operator fun plus(reifiedUnit: ReifiedUnit<U, RU>): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun minus(reifiedUnit: ReifiedUnit<U, RU>): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun unaryPlus(): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun unaryMinus(): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun times(multiplier: Double): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun times(reifiedUnit: ReifiedUnit<U, RU>): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun div(divisor: Double): ReifiedUnit<U, RU>
	/**
	 * non-mutating
	 */
	abstract operator fun div(reifiedUnit: ReifiedUnit<U, RU>): ReifiedUnit<U, RU>
	abstract override operator fun compareTo(other: ReifiedUnit<U, RU>): Int
	abstract override fun toString(): String
	abstract override fun equals(other: Any?): Boolean
	abstract override fun hashCode(): Int

	//
	// Number
	//
	override fun toByte() = value.toInt().toByte()
	override fun toDouble() = value
	override fun toFloat() = value.toFloat()
	override fun toInt() = value.toInt()
	override fun toLong() = value.toLong()
	override fun toShort() = value.toInt().toShort()
}