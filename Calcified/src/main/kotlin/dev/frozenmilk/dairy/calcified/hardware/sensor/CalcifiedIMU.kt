package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.bosch.BHI260IMU
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMU.Register
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.hardware.I2cAddr
import com.qualcomm.robotcore.hardware.ImuOrientationOnRobot
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.LynxModuleImuType.BHI260
import com.qualcomm.robotcore.hardware.LynxModuleImuType.BNO055
import com.qualcomm.robotcore.hardware.LynxModuleImuType.NONE
import com.qualcomm.robotcore.hardware.LynxModuleImuType.UNKNOWN
import com.qualcomm.robotcore.hardware.QuaternionBasedImuHelper.FailedToRetrieveQuaternionException
import com.qualcomm.robotcore.hardware.TimestampedData
import dev.frozenmilk.dairy.calcified.hardware.controller.CompoundSupplier
import dev.frozenmilk.util.angle.Angle
import dev.frozenmilk.util.angle.AngleRadians
import dev.frozenmilk.util.orientation.AngleBasedRobotOrientation
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard
import org.firstinspires.ftc.robotcore.internal.hardware.android.GpioPin
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CalcifiedIMU internal constructor(private val imuType: LynxModuleImuType, private val device: LynxI2cDeviceSynch, initialAngles: AngleBasedRobotOrientation) {
	init {
		when (imuType) {
			NONE, UNKNOWN -> throw IllegalStateException("Attempted to access IMU, but no accessible IMU found")
			BNO055 -> device.i2cAddr = BNO055IMU.I2CADDR_DEFAULT
			BHI260 -> device.i2cAddr = I2cAddr.create7bit(0x28)
		}
	}

	private var offsetOrientation = -initialAngles // todo check, but should be good, as this never gets accessed
	private var previousOrientation = initialAngles
	private var cached = true
	private var cachedTime = System.nanoTime()
	private var previousTime = cachedTime

	/**
	 * the current orientation of the robot
	 */
	var orientation: AngleBasedRobotOrientation = initialAngles
		get() {
			if (!cached) {
				previousOrientation = field
				// doesn't run through the setter function
				val result = readIMU()
				field = result.second - offsetOrientation
			}
			return field
		}
		set(value) {
			offsetOrientation = value - field
			field = value
		}

	/**
	 * the current orientation of the robot
	 */
	var yawPitchRollAngles: YawPitchRollAngles
		get() {
			return orientation.toYawPitchRoll()
		}
		set(value) {
			orientation = fromYawPitchRollAngles(value)
		}

	/**
	 * the heading of the robot, perfectly equivalent to the z axis of the robots rotation
	 */
	var heading: Angle
		get() {
			return orientation.zRot
		}
		set(value) {
			orientation = AngleBasedRobotOrientation(orientation.xRot, orientation.yRot, value)
		}

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the robot's heading
	 */
	val headingSupplier: CompoundSupplier<Angle, Double> = object : CompoundSupplier<Angle, Double> {
		override fun findError(target: Angle): Double {
			return get().intoRadians().findShortestDistance(target)
		}

		override fun get(): Angle {
			return heading
		}
	}

	/**
	 * same as [headingSupplier]
	 */
	val zRotSupplier = headingSupplier

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the angle of the robot around the positive x-axis of the field
	 */
	val xRotSupplier: CompoundSupplier<Angle, Double> = object : CompoundSupplier<Angle, Double> {
		override fun findError(target: Angle): Double {
			return get().intoRadians().findShortestDistance(target)
		}

		override fun get(): Angle {
			return orientation.xRot
		}
	}

	/**
	 * a supplier that can be used in more complex applications of control loops
	 *
	 * supplies the angle of the robot around the positive y-axis of the field
	 */
	val yRotSupplier: CompoundSupplier<Angle, Double> = object : CompoundSupplier<Angle, Double> {
		override fun findError(target: Angle): Double {
			return get().intoRadians().findShortestDistance(target)
		}

		override fun get(): Angle {
			return orientation.yRot
		}
	}

	/**
	 * velocity in radians / second
	 */
	val headingVelocity: Double
		get() {
			return previousOrientation.zRot.intoRadians().findShortestDistance(heading) / (cachedTime - previousTime) / 1E9
		}

	/**
	 * velocity in radians / second
	 */
	val headingVelocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun get(): Double {
			return headingVelocity
		}
	}

	/**
	 * velocity in radians / second
	 */
	val zRotVelocity: Double
		get() {
			return headingVelocity
		}

	/**
	 * velocity in radians / second
	 */
	val zRotVelocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		override fun findError(target: Double): Double {
			return headingVelocitySupplier.findError(target)
		}

		override fun get(): Double {
			return headingVelocitySupplier.get()
		}
	}

	/**
	 * velocity in radians / second
	 */
	val xRotVelocity: Double
		get() {
			return previousOrientation.xRot.intoRadians().findShortestDistance(orientation.xRot) / (cachedTime - previousTime) / 1E9
		}

	/**
	 * velocity in radians / second
	 */
	val xRotVelocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun get(): Double {
			return xRotVelocity
		}
	}

	/**
	 * velocity in radians / second
	 */
	val yRotVelocity: Double
		get() {
			return previousOrientation.yRot.intoRadians().findShortestDistance(orientation.yRot) / (cachedTime - previousTime) / 1E9
		}

	/**
	 * velocity in radians / second
	 */
	val yRotVelocitySupplier: CompoundSupplier<Double, Double> = object : CompoundSupplier<Double, Double> {
		override fun findError(target: Double): Double {
			return target - get()
		}

		override fun get(): Double {
			return yRotVelocity
		}
	}

	/**
	 * lets the imu know to perform a read next time it is queried
	 */
	fun clearCache() {
		cached = false
	}

	/**
	 * performs the actual read of the imu
	 */
	private fun readIMU(): Pair<Quaternion, AngleBasedRobotOrientation> {
		previousTime = cachedTime
		cachedTime = System.nanoTime()
		return when (imuType) {
			NONE, UNKNOWN -> throw IllegalStateException("Attempted to access IMU, but no accessible IMU found")

			BNO055 -> {
				val data: TimestampedData = device.readTimeStamped(Register.QUA_DATA_W_LSB.bVal.toInt(), 8)

				var receivedAllZeros = true
				for (b in data.data) {
					if (b != 0.toByte()) {
						receivedAllZeros = false
						break
					}
				}

				if (receivedAllZeros) {
					// All zeros is not a valid quaternion.
					throw FailedToRetrieveQuaternionException()
				}

				val buffer = ByteBuffer.wrap(data.data).order(ByteOrder.LITTLE_ENDIAN)
				val quaternion = Quaternion(buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, data.nanoTime)
				quaternion to fromQuaternion(quaternion)
			}

			BHI260 -> {
				if (gameRVRequestGpio !is GpioPin) {
					// We must be running on a CH OS older than 1.1.3, there's no sense wasting time trying
					// to read a value.
					throw FailedToRetrieveQuaternionException()
				}

				//todo removed some async protection stuff, may cause issues
				gameRVRequestGpio.setState(true)
				val timestamp: Long = System.nanoTime()
				// We need to wait at least 500 microseconds before performing the I2C read. Fortunately
				// for us, that amount of time has already passed by the time that the internal LynxModule
				// finishes receiving the I2C read command.

				/**
				 * 0x32 is the target register, see [com.qualcomm.hardware.bosch.BHI260IMU.Register.GEN_PURPOSE_READ]
				 */
				val data: ByteBuffer = ByteBuffer.wrap(device.read(0x32, 8)).order(ByteOrder.LITTLE_ENDIAN)

				gameRVRequestGpio.setState(false)

				val xInt = data.short.toInt()
				val yInt = data.short.toInt()
				val zInt = data.short.toInt()
				val wInt = data.short.toInt()

				if (xInt == 0 && yInt == 0 && zInt == 0 && wInt == 0) {
					// All zeros is not a valid quaternion.
					throw FailedToRetrieveQuaternionException()
				}

				val x = (xInt * QUATERNION_SCALE_FACTOR).toFloat()
				val y = (yInt * QUATERNION_SCALE_FACTOR).toFloat()
				val z = (zInt * QUATERNION_SCALE_FACTOR).toFloat()
				val w = (wInt * QUATERNION_SCALE_FACTOR).toFloat()
				val quaternion = Quaternion(w, x, y, z, timestamp)
				quaternion to fromQuaternion(quaternion)
			}
		}
	}

	private companion object {
		/*
		constant values from the BHI260 IMU
		 */

		val QUATERNION_SCALE_FACTOR: Double = BHI260IMU::class.java.getDeclaredField("QUATERNION_SCALE_FACTOR").get(null) as Double

		// We want these fields to get initialized even if initialization ends up failing
		val gameRVRequestGpio = AndroidBoard.getInstance().bhi260Gpio5

		/*
		constant values from the BNO055 IMU
		 */

		const val scale: Float = (1 shl 14).toFloat()
	}
}

/**
 * makes an [AngleBasedRobotOrientation] from an [Orientation]
 */
fun fromOrientation(orientation: Orientation): AngleBasedRobotOrientation {
	val formattedOrientation = orientation
			.toAxesReference(AxesReference.EXTRINSIC)
			.toAxesOrder(AxesOrder.XYZ)
			.toAngleUnit(AngleUnit.RADIANS)
	return AngleBasedRobotOrientation(
			AngleRadians(formattedOrientation.firstAngle.toDouble()),
			AngleRadians(formattedOrientation.secondAngle.toDouble()),
			AngleRadians(formattedOrientation.thirdAngle.toDouble())
	)
}

/**
 * makes an [AngleBasedRobotOrientation] from an [ImuOrientationOnRobot]
 *
 * useful when defining an imu from a [RevHubOrientationOnRobot]
 */
fun fromImuOrientationOnRobot(imuOrientationOnRobot: ImuOrientationOnRobot): AngleBasedRobotOrientation {
	return fromQuaternion(imuOrientationOnRobot.imuCoordinateSystemOrientationFromPerspectiveOfRobot())
}

/**
 * makes an [AngleBasedRobotOrientation] from a [Quaternion]
 */
fun fromQuaternion(quaternion: Quaternion): AngleBasedRobotOrientation {
	// this probably sucks and is slow but eh, can't really be that bad, and saves me from quaternion hell
	return fromOrientation(quaternion.toOrientation(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.RADIANS))
}

fun fromYawPitchRollAngles(yawPitchRollAngles: YawPitchRollAngles): AngleBasedRobotOrientation {
	return fromOrientation(Orientation(AxesReference.INTRINSIC, AxesOrder.ZXY, AngleUnit.DEGREES, yawPitchRollAngles.getYaw(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.getPitch(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.getRoll(AngleUnit.DEGREES).toFloat(), yawPitchRollAngles.acquisitionTime))
}

fun AngleBasedRobotOrientation.toOrientation(): Orientation {
	return Orientation(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.RADIANS, xRot.intoRadians().theta.toFloat(), yRot.intoRadians().theta.toFloat(), zRot.intoRadians().theta.toFloat(), 0L)
}

fun AngleBasedRobotOrientation.toYawPitchRoll(): YawPitchRollAngles {
	val orientation = toOrientation().toAxesOrder(AxesOrder.ZXY).toAxesReference(AxesReference.EXTRINSIC)
	return YawPitchRollAngles(orientation.angleUnit, orientation.firstAngle.toDouble(), orientation.secondAngle.toDouble(), orientation.thirdAngle.toDouble(), orientation.acquisitionTime)
}
