package dev.frozenmilk.dairy.calcified.hardware.motor

enum class Direction(val multiplier: Byte) {
	FORWARD(1),
	REVERSE(-1)
}
