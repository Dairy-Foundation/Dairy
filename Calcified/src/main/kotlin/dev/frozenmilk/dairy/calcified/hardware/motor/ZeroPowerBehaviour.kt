package dev.frozenmilk.dairy.calcified.hardware.motor

import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior

/**
 * removes the strange UNKNOWN and allows for potential renaming / extensibility
 */
enum class ZeroPowerBehaviour(internal val wrapping: ZeroPowerBehavior) {
	FLOAT(ZeroPowerBehavior.FLOAT),
	BRAKE(ZeroPowerBehavior.BRAKE)
}