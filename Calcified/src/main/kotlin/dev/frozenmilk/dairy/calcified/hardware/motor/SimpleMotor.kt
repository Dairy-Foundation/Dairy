package dev.frozenmilk.dairy.calcified.hardware.motor

interface SimpleMotor {
	var direction: Direction
	var cachingTolerance: Double
	var enabled: Boolean
	var power: Double
	// sets the power, ignoring the caching tolerance
	fun forcePower(power: Double) {
		val tolerance = cachingTolerance
		cachingTolerance = 0.0
		this.power = power
		cachingTolerance = tolerance
	}
}