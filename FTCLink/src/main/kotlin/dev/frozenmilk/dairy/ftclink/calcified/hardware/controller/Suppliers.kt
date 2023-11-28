package dev.frozenmilk.dairy.ftclink.calcified.hardware.controller

import java.util.function.Supplier

@FunctionalInterface
interface ErrorSupplier<T> {
	fun getError(target: T): Double
}

interface CompoundSupplier<T> : ErrorSupplier<T>, Supplier<T> {
	fun clearCache()
}

//class VelocityPacket(val deltaSeconds: Double, val deltaPosition: Double) {
//	val velocity = lazy {
//		deltaPosition / deltaSeconds
//	}
//}