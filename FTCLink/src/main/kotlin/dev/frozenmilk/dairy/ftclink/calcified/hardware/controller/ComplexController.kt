package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import dev.frozenmilk.dairy.ftclink.calcified.hardware.CalcifiedMotor
import java.util.function.Supplier

interface ComplexController<IN> {
	var target: IN
	val motors: Set<CalcifiedMotor>
	val calculate: Supplier<Double>
}

@FunctionalInterface
interface CalculationComponent<IN> {
	fun calculate(input: IN): Double
}

class LambdaController<IN> private constructor(override var target: IN, override val motors: Set<CalcifiedMotor>, override val calculate: Supplier<Double>) : ComplexController<IN> {
	init {
		TODO("register weakref of this with the marrowmap")
	}

	/**
	 * constructs a new Controller that always outputs 0
	 */
	constructor(target: IN) : this(target, setOf(), { 0.0 })

	fun addMotors(vararg motors: CalcifiedMotor): LambdaController<IN> = LambdaController(this.target, setOf(*motors), this.calculate)

	fun <OUT> appendErrorBasedController(errorSupplier: ErrorSupplier<IN, OUT>, calculationComponent: CalculationComponent<OUT>) = LambdaController(this.target, this.motors) { calculate.get() + calculationComponent.calculate(errorSupplier.getError(target)) }

	fun appendPController(errorSupplier: ErrorSupplier<IN, Double>, kP: Double) = appendErrorBasedController(errorSupplier, object : CalculationComponent<Double> {
		override fun calculate(input: Double): Double {
			return input * kP
		}
	})
}