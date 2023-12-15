package dev.frozenmilk.dairy.calcified.hardware.motor

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

interface ComplexMotor : SimpleMotor {
	var zeroPowerBehaviour: ZeroPowerBehaviour
	fun getCurrent(unit: CurrentUnit): Double
}