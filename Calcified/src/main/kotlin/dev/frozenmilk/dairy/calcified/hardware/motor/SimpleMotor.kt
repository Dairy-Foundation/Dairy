package dev.frozenmilk.dairy.calcified.hardware.motor

interface SimpleMotor {
	var direction: Direction
	var cachingTolerance: Double
	var enabled: Boolean
	var power: Double
}