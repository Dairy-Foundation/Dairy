package dev.frozenmilk.dairy.ftclink.calcified.hardware

import dev.frozenmilk.dairy.ftclink.geometry.angle.Angle
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleDegrees
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleRadians
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.ErrorSupplier
import java.util.function.Supplier

abstract class EncoderConverter<T>(var ticksPerT: Double, var encoder: CalcifiedEncoder) : ErrorSupplier<T>, Supplier<T>

abstract class AngleEncoder(ticksPerT: Double, encoder: CalcifiedEncoder) : EncoderConverter<Angle>(ticksPerT, encoder) {
	/**
	 * returns in angle type of this, not target
	 * if this is AngleDegreesEncoder, result is in the domain [-180, 180]
	 * if this is AngleRadiansEncoder, result is in the domain [-PI, PI]
	 */
	override fun getError(target: Angle): Double {
		return get().findShortestDistance(target)
	}
}

class AngleDegreesEncoder(ticksPerDegree: Double, encoder: CalcifiedEncoder) : AngleEncoder(ticksPerDegree, encoder) {
	override fun get(): AngleDegrees {
		return AngleDegrees(encoder.getTicks() / ticksPerT)
	}
}

class AngleRadiansEncoder(ticksPerRadian: Double, encoder: CalcifiedEncoder) : AngleEncoder(ticksPerRadian, encoder) {
	override fun get(): AngleRadians {
		return AngleRadians(encoder.getTicks() / ticksPerT)
	}
}
