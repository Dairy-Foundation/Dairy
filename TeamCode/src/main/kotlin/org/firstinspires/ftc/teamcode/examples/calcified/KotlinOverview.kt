package org.firstinspires.ftc.teamcode.examples.calcified

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.PwmControl
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.core.util.supplier.conditionalBind
import dev.frozenmilk.dairy.calcified.hardware.controller.AngularFFController
import dev.frozenmilk.dairy.calcified.hardware.controller.DController
import dev.frozenmilk.dairy.calcified.hardware.controller.IController
import dev.frozenmilk.dairy.calcified.hardware.controller.LinearControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.MotionProfile
import dev.frozenmilk.dairy.calcified.hardware.controller.PController
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.calcified.hardware.motor.ZeroPowerBehaviour
import dev.frozenmilk.dairy.calcified.hardware.sensor.fromImuOrientationOnRobot
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation
import dev.frozenmilk.util.profile.ProfileConstraints
import dev.frozenmilk.util.profile.ProfileStateComponent
import dev.frozenmilk.util.units.distance.DistanceUnits
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.distance.Distance
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

@TeleOp
@Calcified.Attach( // attaches the Calcified feature
		automatedCacheHandling = true, // these are settings for the feature that we can set
		crossPollinate = true, // setting both to true is the default, but if you're a more advanced user you may want to make use of these
)
// @DairyCore
// can also be used to activate all dairy library features, but doesn't allow settings,
// also, if @DairyCore is present it will clash with the @Calcified.Attach annotation
class KotlinOverview : OpMode() {
	init {
		// this ensures that Calcified is attached,
		// if it failed for some reason, then it will spit out a helpful error describing why
		// what you asked for wasn't successfully attached

		// if this line isn't here, the first time you run an opmode with Calcified in it it might crash,
		// and then work after that, due to the way classes are loaded in java,
		// so this line is advised even if you know that everything should be fine
		FeatureRegistrar.checkFeatures(this, Calcified)
	}

	// fields which are used as demo, ignore these for the moment and come back to them when later comments refer to them
	private lateinit var motor1: CalcifiedMotor
	private val motor2 by OpModeLazyCell {
		val motor = Calcified.controlHub.getMotor(2)
		motor.direction = Direction.REVERSE
		motor.cachingTolerance = 0.01
		motor.zeroPowerBehaviour = ZeroPowerBehaviour.BRAKE
		motor
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
		// note: this port is out of bounds
		val unitsEncoder = Calcified.controlHub.getDistanceEncoder(10, 100.0, DistanceUnits.INCH)

		// additionally, the radians and degrees encoders wrap into absolute degree measurements
		val radEncoder = Calcified.controlHub.getRadianEncoder(1, 300.0) // the ticks per revolution let the encoder know how to derive the number of ticks in a radian
		val degEncoder = Calcified.controlHub.getDegreeEncoder(2, 300.0) // the ticks per revolution let the encoder know how to derive the number of ticks in a degree

		// both of these are the same type, and, if you have your own custom wrappingUnit implementation, you can use that as well
		val customisedEncoder = Calcified.controlHub.getAngleEncoder(3, 1000.0, AngleUnits.RADIAN)

		// all Angles, custom or not, can easily be converted
		val radians: Angle = radEncoder.position
		val degrees: Angle = radians.intoDegrees()
		val radiansAgain: Angle = degrees.into(AngleUnits.RADIAN)

		// encoders offer two big pieces of information pre-built into them
		// the position supplier
		encoder.positionSupplier
		// the position itself
		encoder.position // these are the same
		encoder.positionSupplier.get()
		// the position can also be set
		encoder.position = 100
		// the encoder will now measure as though it really was at position 100 there

		// and the velocity supplier
		encoder.velocitySupplier
		// the velocity itself
		encoder.velocity // these are the same
		encoder.velocitySupplier.get()

		// both of which are pre-wrapped with the capability to automatically calculate error
		encoder.positionSupplier.findError(100) // how far away is the encoder from 100 ticks?
		encoder.velocitySupplier.findError(54.3) // how far away is the encoder from a velocity of 54.3 ticks / second?

		// for other types of encoders, these properties stay true
		degEncoder.positionSupplier.findError(Angle(AngleUnits.DEGREE, 180.0)) // how far away is the encoder from 180 degrees?
		degEncoder.velocitySupplier.findError(1000.0) // how far away is the encoder from 1000 degrees per second?

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
		val imu = Calcified.controlHub.getIMU(2, LynxModuleImuType.BNO055, AngleBasedRobotOrientation(Angle(AngleUnits.DEGREE, 50.0), Angle(AngleUnits.DEGREE, -40.7), Angle()))
		val fast_imu = Calcified.controlHub.getIMU(4)

		// the imu supports the sdk's default way of doing angles
		imu.yawPitchRollAngles

		imu.heading // the heading of the robot
		// the angles from the robot orientation
		imu.orientation.xRot
		imu.orientation.yRot
		imu.orientation.zRot // this is the same as the heading

		// like the encoders, the imu supports some complex suppliers
		imu.headingSupplier.get() // the heading of the robot, same as imu.heading
		imu.headingSupplier.findError(Angle(AngleUnits.DEGREE, 50.0)) // how far away is the heading of the robot from 50 degrees?

		imu.xRotSupplier
		imu.yRotSupplier
		imu.zRotSupplier // the same the as the heading supplier

		// setting the imu's orientation does so for all the axes
		imu.orientation = AngleBasedRobotOrientation(Angle(AngleUnits.DEGREE, 90.0), Angle(), Angle())

		// the above is equivalent to the below, due to how frequently teams only use the heading
		imu.heading = Angle(AngleUnits.DEGREE, 90.0)

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
		digitalInput.enhanced.onTrue

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
		// Calcified offers powerful motor controllers instead of the run to position, and run with encoders run modes
		// we use the generic parameter to specify the the target type of the system
		// we provide support for number targets, and angle targets
		// but its pretty easy to write your own!
		val controller = LinearControllerCompiler<Int>()
				// attaches a mix of motors and cr servos to be updated by the controller
				.add(motor, motor1, motor2, crServo)
				// says to use the position supplier on the encoder for each of the following
				.withErrorSupplier(encoder.positionSupplier, true)
				// adds a P term
				.append(PController(0.1))
				// adds an I term
				.append(IController(0.0001, 0.0, 0.2))
				// adds a D term
				.append(DController(0.005))
				.compile(0, 0.002)

		// causes this controller to automatically have .update() called on it, do this after finishing building the whole controller,
		// and turn it off again before making further changes
		// this method updates the controller by hand, if auto update is off, this needs to be called for the controller to run
		controller.update()
		// the target of the controller can be changed simply:
		controller.target = 1000

		controller.error()
		controller.error(100)
		controller.finished()
		controller.finished(0.005)

		val armController = LinearControllerCompiler<Angle>()
				.withPositionSupplier(degEncoder.positionSupplier)
				// an arm FF controller can only be appended to a controller that takes in an Angle as a target
				// and runs the angle the supplier through the cos function, which means if you set up your encoder correctly,
				// this will provide full power when the arm is out flat, and none when its vertical
				.append(AngularFFController(0.1))
				// notice that the .new() method can be called on any Unit implementation to make a new one of it as well
				.compile(Angle(AngleUnits.DEGREE, 0.0), 0.2)

		// motion profiles can also be used in controllers!
		val profiledController = LinearControllerCompiler<Int>()
				.withPositionSupplier(encoder.positionSupplier)
				.append(MotionProfile(ProfileConstraints(100.0, 10.0), ProfileStateComponent.Position))
				.compile(100, 0.1)

		// todo these
//		val customController = ControllerCompiler<Vector2D>()
//				.withErrorSupplier(ErrorSupplier<Vector2D, Vector2D>)
//				.append()


//		//
//		// Gamepads:
//		//
//		// Calcified also offers advanced versions of the Gamepads
//		Calcified.gamepad1
//		Calcified.gamepad2
//
//		// buttons on the gamepads are represented by EnhancedBooleanSuppliers
//		// which we also saw when looking at digital and analog inputs
//		var enhancedBooleanSupplier = Calcified.gamepad1.a
//
//		enhancedBooleanSupplier.state // current state
//		enhancedBooleanSupplier.whenTrue // a rising edge detector
//		enhancedBooleanSupplier.whenFalse // a falling edge detector
//		enhancedBooleanSupplier.toggleTrue // a toggle that gets changed whenever a rising edge is detected
//		enhancedBooleanSupplier.toggleFalse // a toggle that gets changed whenever a falling edge is detected
//
//		// EnhancedBooleanSuppliers are immutable by default, so you can pull them out of the gamepad, do one-off modifications to them, and then store and use them again later
//
//		// debouncing can be applied independently to both the rising and falling edge
//		// note that each of these operations does not modify the original supplier, attached to gamepad1.a
//		enhancedBooleanSupplier = enhancedBooleanSupplier.debounce(0.1)
//		enhancedBooleanSupplier = enhancedBooleanSupplier.debounce(0.1, 0.0)
//		enhancedBooleanSupplier = enhancedBooleanSupplier.debounceFallingEdge(0.1)
//		enhancedBooleanSupplier = enhancedBooleanSupplier.debounceRisingEdge(0.1)
//
//		// if we do not reassign the new EnhancedBooleanSupplier to the variable, or store it in a different variable it will be lost
//
//		// suppliers can also be combined:
//		enhancedBooleanSupplier = enhancedBooleanSupplier and { encoder.position > 5 }
//		enhancedBooleanSupplier = enhancedBooleanSupplier or { encoder.velocity < 100.0 }
//
//		// this works is all kinds of ways!
//		val twoButtons = Calcified.gamepad1.a and Calcified.gamepad1.b
//
//		// you can also reassign the buttons on the gamepads themselves, if you wish to make a change more global
//		Calcified.gamepad2.a = Calcified.gamepad1.a or Calcified.gamepad2.a
//		// now either the driver or the operator can trigger this condition!
//
//		// note: the calcified gamepads have remaps for all gamepad buttons and inputs, the inputs that are shared across the different gamepad types
//		// but share a name (i.e. cross on a ps4 controller and a on a logitech or x-box controller) are linked together on the calcified gamepad
//
//		// sticks and triggers are represented via EnhancedNumberSuppliers
//		var enhancedNumberSupplier = Calcified.gamepad1.leftStickY
//
//		// the value of the stick
//		enhancedNumberSupplier.get()
//
//		// deadzones can be applied, much like the EnhancedBooleanSupplier, these operations are non-mutating
//		enhancedNumberSupplier = enhancedNumberSupplier.applyDeadzone(0.1) // becomes -0.1, 0.1
//		enhancedNumberSupplier = enhancedNumberSupplier.applyDeadzone(-0.1, 0.2)
//		enhancedNumberSupplier = enhancedNumberSupplier.applyUpperDeadzone(-0.1)
//		enhancedNumberSupplier = enhancedNumberSupplier.applyLowerDeadzone(0.1)
//
//		// EnhancedNumberSuppliers also interact well with building complex EnhancedBooleanSuppliers from ranges
//		val rangeBasedCondition = enhancedNumberSupplier.conditionalBind()
//				.greaterThan(-0.5)
//				.lessThan(0.5)
//				.bind()
//
//		// this system is fairly intuitive, and works best if you list numbers from smallest to largest,
//		// or in pairs e.g.:
//
//		val complexRangeBasedCondition = enhancedNumberSupplier.conditionalBind()
//				.greaterThan(0.0)
//				.lessThan(10.0)
//				.greaterThanEqualTo(1.0)
//				.lessThanEqualTo(1000.0)
//				.bind()

		// forms two acceptable ranges,
		// but obviously this could be simplified
		// imagine you were reading out each condition and then drawing it on the number line.
		// if it can form a closed range with the last condition you listed, it will!

		// for 90% of use cases it is just best to list numbers from smallest to largest, the rest will work itself out

		// this process works for all Supplier<Double>s and so can be used on things like encoders:
		val encoderBasedCondition = encoder.positionSupplier.conditionalBind()
				.greaterThanEqualTo(100)
				.lessThanEqualTo(250)
				.bind()

		// Distance encoders work too (all units implement Number)
		val distanceEncoderBasedPosition = unitsEncoder.positionSupplier.conditionalBind()
				.lessThan(Distance(DistanceUnits.FOOT, 1.2))
				.bind()

		// remember, it is best to run these operations once at the start of the op mode, and store them for later,
		// as they are reasonably expensive to remake every loop
		// but checking
		encoderBasedCondition.onTrue
		// will run all the correct checks against the encoder position whenever you call it, but only if you call it

		// Hopefully this has been a helpful overview of how to use Calcified, hosted on DairyCore
		// See the other examples for better usage examples of these features (if there are no other examples uhh, this should be fine tbh, and todo)
	}

	override fun loop() {
	}
}