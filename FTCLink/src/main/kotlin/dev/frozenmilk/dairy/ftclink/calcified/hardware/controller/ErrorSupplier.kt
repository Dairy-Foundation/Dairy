package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

@FunctionalInterface
interface ErrorSupplier<T> {
	fun getError(target: T): Double
}