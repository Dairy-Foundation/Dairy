package dev.frozenmilk.dairy.ftclink

import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import kotlin.math.abs

class CalcifiedMotor {
    var zeroPowerBehavior = ZeroPowerBehavior.FLOAT
    var direction = Direction.FORWARD
    var cachingTolerance = 0.02
    var enabled = true

    var power = 0.0
        get() = if (enabled) field else 0.0
        set(value) {
            field = if (abs(power - value) > cachingTolerance) {
                TODO("send power command")
            } else { power }
        }

    fun enable() { enabled = true }
    fun disable() { enabled = false }

    enum class Direction(val multiplier: Double) {
        FORWARD(1.0),
        REVERSE(-1.0)
    }
}