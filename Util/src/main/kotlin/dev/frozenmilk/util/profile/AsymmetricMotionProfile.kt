package dev.frozenmilk.util.profile

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import dev.frozenmilk.util.profile.ProfileStateComponent.*

class AsymmetricMotionProfile(initial: Double, final: Double, val constraints: ProfileConstraints) {
    val totalTime: Double
    val distance: Double
    val original: Double

    val initial: Double
    val final: Double

    var t1: Double
    var t2: Double
    var t3: Double

    val t1Stop: Double
    val t2Stop: Double

    val maxVelocity: Double

    val flipped = final < initial

    init {
        val (vel, accel, decel) = constraints

        if (final < initial) {
            original = initial

            // TODO: check if this really swaps i wrote this when i was sleep deprived
            this.initial = final.also { this.final = initial }
        } else {
            original = 0.0

            this.initial = initial
            this.final = final
        }

        distance = final - initial

        t1 = vel / accel
        t3 = vel / decel
        t2 = abs(distance) / vel - (t1 + t3) / 2.0

        if (t2 < 0.0) {
            t2 = 0.0

            val a = (accel / 2.0) * (1 - accel / -decel)
            val c = -distance

            t1 = sqrt(-4 * a * c) / (2 * a)
            t3 = -(accel * t1) / -decel

            maxVelocity = accel * t1

            t1Stop = accel * t1.pow(2.0) / 2.0
            t2Stop = t1Stop
        } else {
            maxVelocity = vel

            t1Stop = vel * t1 / 2.0
            t2Stop = t1Stop + t2 * maxVelocity
        }

        totalTime = t1 + t2 + t3
    }

    fun calculate(time: Double, component: ProfileStateComponent) = when (component) {
        Position -> calculate(time).x
        Velocity -> calculate(time).v
        Acceleration -> calculate(time).a
    }

    fun calculate(time: Double): ProfileState {
        val position: Double
        val velocity: Double
        val acceleration: Double
        val stage: Double

        when {
            time <= t1 -> {
                stage = time
                acceleration = constraints.accel
                velocity = acceleration * stage
                position = velocity * stage / 2.0
            }

            time <= t1 + t2 -> {
                stage = time - t1
                acceleration = 0.0
                velocity = constraints.velocity
                position = t1Stop + stage * velocity
            }

            time <= totalTime -> {
                stage = time - t1 - t2
                acceleration = -constraints.decel
                velocity = maxVelocity - stage * constraints.decel
                position = t2Stop + (maxVelocity + velocity) / 2.0 * stage
            }

            else -> {
                acceleration = 0.0
                velocity = 0.0
                position = final
            }
        }

        val x: Double = if (time <= totalTime) {
            if (flipped) original - position else initial + position
        } else {
            if (flipped) initial else original + position
        }

        return ProfileState(x, velocity, acceleration)
    }
}