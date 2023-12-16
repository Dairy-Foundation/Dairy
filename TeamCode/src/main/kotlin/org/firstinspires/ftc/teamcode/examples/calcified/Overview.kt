package org.firstinspires.ftc.teamcode.examples.calcified

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.PwmControl
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.Calcify
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.calcified.hardware.motor.ZeroPowerBehaviour
import dev.frozenmilk.dairy.calcified.hardware.sensor.fromImuOrientationOnRobot
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeLazyCell
import dev.frozenmilk.util.angle.AngleDegrees
import dev.frozenmilk.util.orientation.AngleBasedRobotOrientation
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

@TeleOp
@Calcify(
		automatedCacheHandling = true, // these are settings for the feature that we can set
		crossPollinate = true, // setting both to true is the default, but if you're a more advanced user you may want to make use of these
)
class Overview : OpMode() {
	// fields which are used as demo, ignore these for the moment and come back to them when later comments refer to them
	lateinit var motor1: CalcifiedMotor
	val motor2 by OpModeLazyCell {
		val motor = Calcified.controlHub.getMotor(2)
		motor.direction = Direction.REVERSE
		motor.cachingTolerance = 0.01
		motor.zeroPowerBehaviour = ZeroPowerBehaviour.BRAKE
		motor
	}
	init {
		// this ensures that Calcified is attached,
		// if it failed for some reason, then it will spit out a helpful error describing why
		// what you asked for wasn't successfully attached

		// if this line isn't here, the first time you run an opmode with calcified in it it might crash,
		// and then work after that, due to the way classes are loaded in java,
		// so this line is advised even if you know that everything should be fine
		FeatureRegistrar.checkFeatures(this, Calcified)
	}
	override fun init() {
		// Calcified provides access to the control and expansion hubs like so:
		Calcified.controlHub
		Calcified.expansionHub
		// if one of them isn't plugged in electronically, accessing it will throw an error

		// from now on, the control hub will be used to show accessing hardware objects, the two hubs are functionally equivalent

		// motors and similar hardware objects can be written as lateinit var fields as usual, and then inited in this init block
		// for example, motor0
		motor1 = Calcified.controlHub.getMotor(0)
		motor1.zeroPowerBehaviour = ZeroPowerBehaviour.BRAKE

		// if you are going to do more complex set up operations, it can be nice to instead bundle your initialisation into an OpModeLazyCell, like motor1
		// even if i don't access motor1 here, its contents get evaluated, which prevents issues from relying on lazy initialisation, and allows more complex
		// initialisation sequences to bundled onto the motor itself, rather than clogging up the init block
		// this allows a whole bunch of declarative code to be set up in these cells, especially if you're using calcified motor controllers,
		// which use dairy core to automatically update themselves, like RUN_TO_POSITION

		// Now that we've reviewed initialisation, we'll review the hardware objects in calcified
		//
		// MOTORS:
		//
		val motor = Calcified.controlHub.getMotor(0)
		motor.zeroPowerBehaviour = ZeroPowerBehaviour.BRAKE // pretty self explanatory, the same as the option in the sdk
		motor.direction = Direction.FORWARD // once again
		// determines the level of difference between one value and the next before a write is performed
		motor.cachingTolerance = 0.005 // you might want to lower it for more fine grained control, or raise it for less
		// the lower it is, the slower your loops will be, and visa versa

		motor.enabled = true // if set to false, will stop the motor from moving, matches the sdk
		motor.power = 1.0 // same as the sdk, does some more checks first to see if it should write first though
		motor.getCurrent(CurrentUnit.AMPS) // same as the sdk
		// and thats it for motors, you might be thinking, what happened to encoders?, or run modes?
		// calcified does some major reorganising of ideas here, but offers more powerful alternatives

		//
		// ENCODERS:
		//
		// encoders come in several flavours
		// the ticks encoder is the standard variant
		val encoder = Calcified.controlHub.getTicksEncoder(0)
		// additionally, the radians and degrees encoders wrap into absolute degree measurements
		val radEncoder = Calcified.controlHub.getRadiansEncoder(1, 300.0) // the ticks per revolution let the encoder know how to derive the number of ticks in a radian
		val degEncoder = Calcified.controlHub.getDegreesEncoder(2, 300.0) // the ticks per revolution let the encoder know how to derive the number of ticks in a degree
		// don't worry too much about which of these you want to use, most most of the time you'll probably want the RadiansEncoder
		// but their outputs can be easily converted
		val radians = radEncoder.getPosition()
		val degrees = radians.intoDegrees()

		// encoders offer two big pieces of information pre-built into them
		// the position supplier
		encoder.positionSupplier
		// the position itself
		encoder.getPosition() // these are the same
		encoder.positionSupplier.get()

		// and the velocity supplier
		encoder.velocitySupplier
		// the velocity itself
		encoder.getVelocity() // these are the same
		encoder.velocitySupplier.get()

		// both of which are pre-wrapped with the capability to automatically calculate error
		encoder.positionSupplier.getError(100) // how far away is the encoder from 100 ticks?
		encoder.velocitySupplier.getError(54.3) // how far away is the encoder from a velocity of 54.3 ticks / second?

		// for other types of encoders, these properties stay true
		degEncoder.positionSupplier.getError(AngleDegrees(180.0)) // how far away is the encoder from 180 degrees?
		degEncoder.velocitySupplier.getError(1000.0) // how far away is the encoder from 1000 degrees per second?

		// but these features probably won't come into handy too often for you
		// instead they allow encoders to interface super nicely with the motor controller interfaces, which we'll review later

		//
		// Servos:
		//
		// servos work much like servos in the sdk:
		val servo = Calcified.controlHub.getServo(0)
		servo.direction = Direction.FORWARD
		servo.enabled = true
		servo.cachingTolerance = 0.001 // same as the motor, but for changes in position
		servo.pwmRange = PwmControl.PwmRange.defaultRange // this version supplies immediate access the pwm range object, used to control the servo positions
		servo.position = 0.0 // similar to the motor, performs similarly to the set position on a servo from the base sdk, but does some more checks before it writes

		// everything on the crservo performs the same as it does on the motor and / or servo
		val crServo = Calcified.controlHub.getContinuousServo(1)
		crServo.direction = Direction.FORWARD
		crServo.enabled = true
		crServo.cachingTolerance = 0.005
		crServo.pwmRange = PwmControl.PwmRange.defaultRange
		crServo.power = 0.0

		//
		// IMUs:
		//
		// there are several ways to initialise an imu, supporting both old ways of initialising IMUs and new ones
		// remember, the IMU built into the REV Control / Expansion Hubs are in port 0
		val imu_BHI260 = Calcified.controlHub.getIMU_BHI260(0)
		val imu_BNO055 = Calcified.controlHub.getIMU_BNO055(1, fromImuOrientationOnRobot(RevHubOrientationOnRobot(
				RevHubOrientationOnRobot.LogoFacingDirection.UP,
				RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
		)))
		val imu = Calcified.controlHub.getIMU(2, LynxModuleImuType.BNO055, AngleBasedRobotOrientation(AngleDegrees(50.0), AngleDegrees(-40.7), AngleDegrees()))

		imu.heading // the heading of the robot
		// the angles from the robot orientation
		imu.orientation.xRot
		imu.orientation.yRot
		imu.orientation.zRot // this is the same as the heading

		// like the encoders, the imu supports some complex suppliers
		imu.headingSupplier.get() // the heading of the robot, same as imu.heading
		imu.headingSupplier.getError(AngleDegrees(50.0)) // how far away is the heading of the robot from 50 degrees?

		imu.xRotSupplier
		imu.yRotSupplier
		imu.zRotSupplier // the same the as the heading supplier

		// setting the imu's orientation does so for all the axes
		imu.orientation = AngleBasedRobotOrientation(AngleDegrees(90.0), AngleDegrees(), AngleDegrees())

		// the above is equivalent to the below, due to how frequently teams only use the heading
		imu.heading = AngleDegrees(90.0)

		// velocities can also be obtained
		imu.headingVelocity

		imu.xRotVelocity
		imu.yRotVelocity
		imu.zRotVelocity // the same as heading velocity

		// and their complex supplier forms
		// which can provide error and value
		imu.headingVelocitySupplier
		imu.xRotVelocitySupplier
		imu.yRotVelocitySupplier
		imu.zRotVelocitySupplier // same as the headingVelocitySupplier

		//
		// Digital Channels
		//
		val digitalInput = Calcified.controlHub.getDigitalInput(0)
		digitalInput.get() // the state of the sensor
		digitalInput.enhanced // an enhanced version of the sensor supplier
		// the enhanced version provides several improvements to a normal boolean supplier in the context of writing robot code
		// these features are also provided on the gamepad buttons, and so they'll be covered in depth in that section
		digitalInput.enhanced.whenTrue

		// digital outputs are super simple
		val digitalOutput = Calcified.controlHub.getDigitalOutput(1)
		digitalOutput.accept(true)

		//
		// Analog Sensors
		//
		// analog inputs are the same as digital inputs
		val analogInput = Calcified.controlHub.getAnalogInput(0)
		analogInput.get()
		analogInput.enhanced

		// the rest of the hardware objects are being worked on

		//
		// Motor Controllers
		//
		// Calcified offers powerful motor controllers instead of
	}

	override fun loop() {
	}
}