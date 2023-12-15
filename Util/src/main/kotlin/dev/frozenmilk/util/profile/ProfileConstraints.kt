package dev.frozenmilk.util.profile

data class ProfileConstraints(val velocity: Double, val accel: Double, val decel: Double) {
    constructor(velocity: Double, accel: Double) : this(velocity, accel, accel)

    operator fun times(factor: Double) = ProfileConstraints(velocity * factor, accel * factor, decel * factor)
}