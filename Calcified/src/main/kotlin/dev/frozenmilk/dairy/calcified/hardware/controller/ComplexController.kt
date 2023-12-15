package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.OpModeWrapper
import java.util.function.Supplier

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
