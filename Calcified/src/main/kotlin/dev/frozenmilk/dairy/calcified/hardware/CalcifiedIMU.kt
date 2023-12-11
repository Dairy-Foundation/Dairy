package dev.frozenmilk.dairy.calcified.hardware

import com.qualcomm.hardware.bosch.BHI260IMU
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.BNO055IMU.Register
import com.qualcomm.hardware.bosch.BNO055IMUImpl.VectorData
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch
import com.qualcomm.robotcore.hardware.I2cAddr
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.LynxModuleImuType.*
import com.qualcomm.robotcore.hardware.QuaternionBasedImuHelper.FailedToRetrieveQuaternionException
import com.qualcomm.robotcore.hardware.TimestampedData
import dev.frozenmilk.dairy.calcified.geometry.angle.Angle
import dev.frozenmilk.dairy.calcified.geometry.angle.AngleRadians
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard
import org.firstinspires.ftc.robotcore.internal.hardware.android.GpioPin
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CalcifiedIMU internal constructor(private val imuType: LynxModuleImuType, private val device: LynxI2cDeviceSynch, initialAngles: AngleBasedRobotOrientation) {
	// todo move these elsewhere
//	constructor(initialHeading: Angle) : this(AngleBasedRobotOrientation(zRot = initialHeading))
//	constructor(initialOrientation: Orientation) : this(AngleBasedRobotOrientation(initialOrientation))
//	constructor(initialQuaternion: Quaternion) : this(AngleBasedRobotOrientation(initialQuaternion))
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
	var orientation: AngleBasedRobotOrientation = initialAngles
		get() {
			if (!cached) {
				previousOrientation = field
				// doesn't run through the setter function
				field = readIMU() - offsetOrientation
			}
			return field
		}
		set(value) {
			offsetOrientation = value - field
			field = value
		}

	var heading: Angle
		get() {
			return orientation.zRot
		}
		set(value) {
			orientation = AngleBasedRobotOrientation(orientation.xRot, orientation.yRot, value)
		}

	internal fun clearCache() {
		cached = false
	}

	/**
	 * performs the actual read of the imu
	 */
	private fun readIMU(): AngleBasedRobotOrientation {
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
				AngleBasedRobotOrientation(Quaternion(buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, buffer.getShort() / scale, data.nanoTime))
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
				AngleBasedRobotOrientation(Quaternion(w, x, y, z, timestamp))
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

		val scale: Float = (1 shl 14).toFloat()
	}
}

/**
 * see [rotations using the right hand rule](https://en.wikipedia.org/wiki/Right-hand_rule#Rotations)
 *
 * @property xRot the rotation of the robot about the positive x-axis of the field
 * @property yRot the rotation of the robot about the positive y-axis of the field
 * @property zRot the rotation of the robot about the positive z-axis of the field
 */
class AngleBasedRobotOrientation(val xRot: Angle = AngleRadians(), val yRot: Angle = AngleRadians(), val zRot: Angle = AngleRadians()) {
	constructor(orientation: Orientation) : this(fromOrientation(orientation))

	constructor(quaternion: Quaternion) : this(fromQuaternion(quaternion))

	/**
	 * used by the helper
	 */
	private constructor(o: AngleBasedRobotOrientation) : this(o.xRot, o.yRot, o.zRot)

	/**
	 * non-mutating
	 */
	operator fun plus(other: AngleBasedRobotOrientation): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(this.xRot + other.xRot, this.yRot + other.yRot, this.zRot + other.zRot)
	}

	/**
	 * non-mutating
	 */
	operator fun minus(other: AngleBasedRobotOrientation): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(this.xRot - other.xRot, this.yRot - other.yRot, this.zRot - other.zRot)
	}

	/**
	 * non-mutating, has no effect
	 */
	operator fun unaryPlus(): AngleBasedRobotOrientation {
		return this
	}

	/**
	 * non-mutating, equal to the inverse rotation about all axis
	 */
	operator fun unaryMinus(): AngleBasedRobotOrientation {
		return AngleBasedRobotOrientation(-this.xRot, -this.yRot, -this.zRot)
	}

	private companion object {
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

		fun fromQuaternion(quaternion: Quaternion): AngleBasedRobotOrientation {
			// this probably sucks and is slow but eh, can't really be that bad, and saves me from quaternion hell
			return fromOrientation(quaternion.toOrientation(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.RADIANS))
		}
	}
}