package dev.frozenmilk.dairy.ftclink.calcified.hardware

import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior

/**
 * removes the strange UNKNOWN and allows for potential renaming / extensibility
 */
enum class ZeroPowerBehaviour(internal val wrapping: ZeroPowerBehavior) {
	FLOAT(ZeroPowerBehavior.FLOAT),
	BRAKE(ZeroPowerBehavior.BRAKE)
}