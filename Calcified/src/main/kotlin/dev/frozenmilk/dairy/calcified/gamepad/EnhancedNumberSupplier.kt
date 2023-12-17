package dev.frozenmilk.dairy.calcified.gamepad

import java.util.function.Supplier

class EnhancedNumberSupplier<N: Number >(private val supplier: Supplier<N>, private val modify: (N) -> N = { x -> x }, private val lowerDeadzone: Double = 0.0, private val upperDeadzone: Double = 0.0) : Supplier<Double> {
	constructor(supplier: Supplier<N>) : this(supplier, { x -> x })

	override fun get(): Double {
		val result = modify(supplier.get()).toDouble()
		if (result < 0.0 && result >= lowerDeadzone) return 0.0
		if (result > 0.0 && result <= upperDeadzone) return 0.0
		return result
	}


	/**
	 * non-mutating
	 */
	fun applyDeadzone(deadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, -(deadzone.coerceAtLeast(0.0)), deadzone.coerceAtLeast(0.0))
	/**
	 * non-mutating
	 */
	fun applyDeadzone(lowerDeadzone: Double, upperDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), upperDeadzone.coerceAtLeast(0.0))
	/**
	 * non-mutating
	 */
	fun applyLowerDeadzone(lowerDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), this.upperDeadzone)
	/**
	 * non-mutating
	 */
	fun applyUpperDeadzone(upperDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, this.lowerDeadzone, upperDeadzone.coerceAtLeast(0.0))
}

fun <N: Number> Supplier<N>.conditionalBind(): Conditional<N> = Conditional(this)
