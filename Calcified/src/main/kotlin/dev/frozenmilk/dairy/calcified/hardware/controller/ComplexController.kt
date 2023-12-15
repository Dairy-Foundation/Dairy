package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.SimpleMotor
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.angle.Angle
import java.util.function.Supplier
import kotlin.math.cos

interface ComplexController<IN> : Feature {
	var target: IN
	val motors: SimpleMotor
	val calculate: Supplier<Double>

	override fun preUserInitHook(opMode: OpModeWrapper) {}

	override fun postUserInitHook(opMode: OpModeWrapper) {
		motors.power = calculate.get()
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
		motors.power = calculate.get()
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
	}

	override fun postUserStartHook(opMode: OpModeWrapper) {
		motors.power = calculate.get()
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
		motors.power = calculate.get()
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {
		motors.power = 0.0
	}
}

class LambdaController<IN> private constructor(override var target: IN, override val motors: SimpleMotor, override val calculate: Supplier<Double>) : ComplexController<IN> {
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this).yieldsTo(Calcified::class.java)

	init {
		FeatureRegistrar.registerFeature(this)
	}

	/**
	 * constructs a new Controller that always outputs 0
	 */
	constructor(target: IN) : this(target, MotorControllerGroup(), { 0.0 })

	fun addMotors(vararg motors: SimpleMotor): LambdaController<IN> = LambdaController(this.target, MotorControllerGroup(*motors), this.calculate)

	fun <OUT> appendErrorBasedController(errorSupplier: ErrorSupplier<IN, OUT>, calculationComponent: (OUT) -> Double) = LambdaController(this.target, this.motors) { calculate.get() + calculationComponent(errorSupplier.getError(target)) }
	fun <OUT> appendPositionBasedController(positionSupplier: Supplier<OUT>, calculationComponent: (OUT) -> Double) = LambdaController(this.target, this.motors) { calculate.get() + calculationComponent(positionSupplier.get()) }

	fun appendPController(errorSupplier: ErrorSupplier<IN, Double>, kP: Double) = appendErrorBasedController(errorSupplier) { input: Double -> input * kP }

	fun appendFFController(positionSupplier: Supplier<Double>, kFF: Double) = appendPositionBasedController(positionSupplier) { input: Double -> input * kFF }

	fun appendAngleFFController(positionSupplier: Supplier<Angle>, kFF: Double) = appendPositionBasedController(positionSupplier) { input: Angle -> kFF * cos(input.intoRadians().theta) }
}