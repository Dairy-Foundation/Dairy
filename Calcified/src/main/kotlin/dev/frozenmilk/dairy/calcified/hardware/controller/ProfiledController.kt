package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.util.profile.AsymmetricMotionProfile
import dev.frozenmilk.util.profile.ProfileStateComponent
import java.util.function.Supplier

fun <IN> ProfiledController(target: IN, motors: SimpleMotor, profile: AsymmetricMotionProfile, component: ProfileStateComponent, time: Supplier<Double>)
    = LambdaController(target, motors) { profile.calculate(time.get(), component) }