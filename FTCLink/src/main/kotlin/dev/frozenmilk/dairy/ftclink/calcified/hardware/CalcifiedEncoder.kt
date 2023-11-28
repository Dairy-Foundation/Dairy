package dev.frozenmilk.dairy.ftclink.calcified.hardware

import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand
import dev.frozenmilk.dairy.ftclink.calcified.CalcifiedModule
import dev.frozenmilk.dairy.ftclink.calcified.hardware.controller.CompoundSupplier
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleDegrees
import dev.frozenmilk.dairy.ftclink.geometry.angle.AngleRadians

abstract class CalcifiedEncoder<T> internal constructor(internal val module: CalcifiedModule, internal val port: Byte) {
	var direction = Direction.FORWARD
	abstract val positionSupplier: CompoundSupplier<T, Double>
	abstract val velocitySupplier: CompoundSupplier<Double, Double>

	fun getPosition(): T {
		return positionSupplier.get()
	}

	fun getVelocity(): Double {
		return velocitySupplier.get()
	}

	fun reset() {
		LynxResetMotorEncoderCommand(module.lynxModule, port.toInt()).send()
	}
}

class TicksEncoder internal constructor(module: CalcifiedModule, port: Byte) : CalcifiedEncoder<Int>(module, port) {
	override val positionSupplier: CompoundSupplier<Int, Double> = object : CompoundSupplier<Int, Double> {
		private var cachedPosition: Int? = null
		private var cachedError: Double? = null

		/**
		 * returns error in ticks, consider wrapping this encoder in a different UnitEncoder to use error with some other, more predictable unit
		 */
		override fun getError(target: Int): Double {
			if (cachedError == null) cachedError = (target - get()).toDouble()
			return cachedError!!
		}

		override fun clearCache() {
			cachedError = null
			cachedPosition = null
		}

		override fun get(): Int {
			if (cachedPosition == null) cachedPosition = module.bulkData.getEncoder(port.toInt()) * direction.multiplier
			return cachedPosition!!
		}
	}

	override val velocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedError: Double? = null
		private var previousPosition = positionSupplier.get()

		override fun getError(target: Double): Double {
			if (cachedError == null) cachedError = target - get()
			return cachedError!!
		}

		override fun clearCache() {
			cachedError = null
			cachedVelocity = null
		}

		/**
		 * the velocity since the last time this method was called
		 */
		override fun get(): Double {
			if (cachedVelocity == null) {
				val result = (positionSupplier.get() - previousPosition).toDouble()
				previousPosition = positionSupplier.get()
				cachedVelocity = result
			}
			return cachedVelocity!!
		}
	}
}

abstract class UnitEncoder<T>(protected val ticksEncoder: TicksEncoder, protected val ticksPerUnit: Double) : CalcifiedEncoder<T>(ticksEncoder.module, ticksEncoder.port)

class RadiansEncoder internal constructor(ticksEncoder: TicksEncoder, ticksPerRevolution: Double) : UnitEncoder<AngleRadians>(ticksEncoder, ticksPerRevolution) {
	override val positionSupplier: CompoundSupplier<AngleRadians, Double> = object : CompoundSupplier<AngleRadians, Double> {
		private var cachedAngle: AngleRadians? = null
		private var cachedError: Double? = null

		override fun getError(target: AngleRadians): Double {
			if (cachedError == null) cachedError = get().findShortestDistance(target)
			return cachedError!!
		}

		override fun clearCache() {
			ticksEncoder.positionSupplier.clearCache()
			cachedAngle = null
			cachedError = null
		}

		override fun get(): AngleRadians {
			if (cachedAngle == null) cachedAngle = AngleRadians(Math.PI * 2 * (ticksEncoder.positionSupplier.get() / ticksPerRevolution) * direction.multiplier)
			return cachedAngle!!
		}
	}

	override val velocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedError: Double? = null
		private var previousPosition = positionSupplier.get()

		override fun getError(target: Double): Double {
			if (cachedError == null) cachedError = target - get()
			return cachedError!!
		}

		override fun clearCache() {
			ticksEncoder.velocitySupplier.clearCache()
			cachedVelocity = null
			cachedError = null
		}

		override fun get(): Double {
			if (cachedVelocity == null) {
				val result = (positionSupplier.get() - previousPosition).theta
				previousPosition = positionSupplier.get()
				cachedVelocity = result
			}
			return cachedVelocity!!
		}
	}
}

class DegreesEncoder internal constructor(ticksEncoder: TicksEncoder, ticksPerRevolution: Double) : UnitEncoder<AngleDegrees>(ticksEncoder, ticksPerRevolution) {
	override val positionSupplier: CompoundSupplier<AngleDegrees, Double> = object : CompoundSupplier<AngleDegrees, Double> {
		private var cachedAngle: AngleDegrees? = null
		private var cachedError: Double? = null

		override fun getError(target: AngleDegrees): Double {
			if (cachedError == null) cachedError = get().findShortestDistance(target)
			return cachedError!!
		}

		override fun clearCache() {
			ticksEncoder.positionSupplier.clearCache()
			cachedAngle = null
			cachedError = null
		}

		override fun get(): AngleDegrees {
			if (cachedAngle == null) cachedAngle = AngleDegrees(360 * (ticksEncoder.positionSupplier.get() / ticksPerRevolution) * direction.multiplier)
			return cachedAngle!!
		}
	}

	override val velocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		private var cachedVelocity: Double? = null
		private var cachedError: Double? = null
		private var previousPosition = positionSupplier.get()

		override fun getError(target: Double): Double {
			if (cachedError == null) cachedError = target - get()
			return cachedError!!
		}

		override fun clearCache() {
			ticksEncoder.velocitySupplier.clearCache()
			cachedVelocity = null
			cachedError = null
		}

		override fun get(): Double {
			if (cachedVelocity == null) {
				val result = (positionSupplier.get() - previousPosition).theta
				previousPosition = positionSupplier.get()
				cachedVelocity = result
			}
			return cachedVelocity!!
		}
	}
}