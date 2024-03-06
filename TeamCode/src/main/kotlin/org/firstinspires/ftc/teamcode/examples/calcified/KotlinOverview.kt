package org.firstinspires.ftc.teamcode.examples.calcified

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.LynxModuleImuType
import com.qualcomm.robotcore.hardware.PwmControl
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.DoubleDComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.DoubleIComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.DoublePComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.UnitDComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.UnitIComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.calculation.UnitPComponent
import dev.frozenmilk.dairy.calcified.hardware.controller.compiler.DoubleControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.controller.compiler.UnitControllerCompiler
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.ZeroPowerBehaviour
import dev.frozenmilk.dairy.calcified.hardware.sensor.fromImuOrientationOnRobot
import dev.frozenmilk.dairy.calcified.hardware.sensor.fromYawPitchRollAngles
import dev.frozenmilk.dairy.calcified.hardware.sensor.toYawPitchRoll
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import dev.frozenmilk.dairy.core.util.current.Current
import dev.frozenmilk.dairy.core.util.current.CurrentUnits
import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit
import dev.frozenmilk.util.units.distance.DistanceUnits
import dev.frozenmilk.util.units.orientation.AngleBasedRobotOrientation
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles

@TeleOp
@Calcified.Attach( // attaches the Calcified feature
		automatedCacheHandling = true, // these are settings for the feature that we can set
)
// @DairyCore
// can also be used to activate all dairy library features, but doesn't allow settings,
// also, if @DairyCore is present it will clash with the @Calcified.Attach annotation
class KotlinOverview : OpMode() {
	// This overview will look into using Calcified,
	// while also covering many features from the Util
	// and Core components of the Dairy Ecosystem.
	// This is due to the fact that Calcified and its hardware APIs
	// make use of these features, and so demonstrating the implementation
	// of them at the same time is effective and easier than coming up with
	// more imaginary scenarios for demonstrating the Utilities

	//
	// Topics:
	//
	// OpModeLazyCells
	// Accessing Features
	// Accessing Hardware
	// Motors
	// Encoders
	// > Units + NumericSuppliers system
	// Servos
	// IMUs
	// Analog Sensors
	// Digital Sensors
	// Conditional + BooleanSuppliers system
	// ComplexControllers

	//
	// OpModeLazyCells
	//
	// OpModeLazyCells are designed to be used in an OpMode
	// they are a delayed eager evaluation system, that help when writing Kotlin
	// Additionally, they better group hardware initialisation behaviour and similar
	// NOTE: OpModeLazyCells automatically deregister themselves at the end of an OpMode
	// if you want to reuse them, OpModeFreshLazyCell exists
	// or, you can manually re-register the cell at the start of an OpMode
	// this should not prove an issue under normal operation

	// this OpModeLazyCell causes the motor in port 0 to be retrieved at the start of init
	val motor0 by OpModeLazyCell {
		Calcified.controlHub.getMotor(0)
	}
	// this gets the motor in port 1, and sets the direction to reverse, and then returns it
	// which is once again evaluated at the start of init
	val motor1 by OpModeLazyCell {
		val motor = Calcified.controlHub.getMotor(1)
		motor.direction = Direction.REVERSE
		motor
	}
	// its safe to use values from an OpModeLazyCell in others!
	// a motor controller group allows you to control multiple motor-like objects as one!
	val motorGroup by OpModeLazyCell {
		MotorControllerGroup(motor0, motor1)
	}

	// OpModeLazyCells are part of a family of utilities known as "Cell"s
	// The remainder of this overview is not aimed at a true implementation for an OpMode
	// and will not cover more of Cells
	// The Cell system will be covered in full documentation, but should be usable now if you are willing to read the kdoc
	// The remainder of the variables will not be declared as fields, as you should have the idea by now

	init {
		// checking to see if the features you care about actually activated
		// can be done using this line:
		// in this case, it checks that Calcified got attached
		FeatureRegistrar.checkFeatures(this, Calcified)
		// this block and line do not need to be included, but may be useful in debugging why the Features you wanted are not attached
	}

	override fun init() {
		// Some Features just use the system to update themselves
		// OpModeLazyCell is an example
		// It eagerly evaluates its contents in opmode init
		// and deregisters its self after opmode ends

		// Other Features are more persistent management systems
		// These are implemented using the singleton pattern (object in Kotlin)
		// and they usually need to activated for an OpMode
		// the @Calcified.Attach annotation lets the FeatureRegistrar (manages features and OpModes)
		// know that when this OpMode runs, Calcified gets attached, and receives updates from it

		// Calcified provides access to the control and expansion hubs like so:
		Calcified.controlHub
		Calcified.expansionHub
		// if one of them isn't plugged in electronically, accessing it will throw an error
		// hardware is accessed from the hardware maps on the hubs, or from the quick access methods on the hubs themselves

		// from now on, the control hub will be used to show accessing hardware objects, the two hubs are functionally equivalent

		//
		// Motors
		//
		// as shown above, Calcified uses port numbers for accessing hardware objects
		// no need for a config file!
		val motor2 = Calcified.controlHub.getMotor(2)
		// most of the api is sensible and translates from the SDK
		motor2.zeroPowerBehaviour = ZeroPowerBehaviour.BRAKE
		motor2.direction = Direction.FORWARD
		motor2.enabled = true
		motor2.power = 1.0
		motor2.current // this is out first look at the units system!

		val current = motor2.current
		// the units system is a quickly extensible, generic system of immutable families of units
		// at the moment, we don't know what unit this is in:
		current.value // the value of current as a double
		// we can find out!
		current.unit

		// this gives us back the equivalent unit in amps, but does not mutate the original variable
		// if current was already in amps, this is a no-op
		current.into(CurrentUnits.AMP).value // now we know that this is in amps!

		// as amps and milli amps are built into the system the following utility methods also exist:
		current.intoAmps()
		current.intoMilliAmps()

		// lots of mathematical operations
		current + current.intoAmps()
		// and comparison operations
		current < current.intoAmps()
		// are defined for the families

		// the units system works by 'pulling up'
		// the right hand side of the operation is converted to match the units of the left hand side of the equation

		// so
		current.intoMilliAmps() + current.intoAmps()
		// we know that this outputs in MilliAmps
		// note that this may not be true for non-built-in unit families

		// it is also possible to very easily define your own units for a family
		// that work alongside predefined ones seamlessly

		// we'll look at that, and the rest of the units system later on

		// determines the level of difference between one value and the next before a write is performed
		motor2.cachingTolerance = 0.005 // you might want to lower it for more fine grained control, or raise it for less
		// the lower it is, the slower your loops will be, and visa versa

		// the over current features are more niche from the sdk, but also supported here
		motor2.overCurrentThreshold = Current(CurrentUnits.AMP, 0.2)
		motor2.overCurrent // true if current is greater than overCurrentThreshold
		// over current detection runs much faster than just checking the current, use it if you can

		// and thats it for motors, you might be thinking, what happened to encoders?, or run modes?
		// Calcified does some major reorganising of ideas here, but offers more powerful alternatives
		// Calcified works only with the RUN_WITHOUT_ENCODER run mode, as it offers its own way of doing PID controllers
		//

		//
		// Encoders
		//
		// encoders are separate from motors in Calcified
		// a ticks encoder is probably what you're accustomed to, it just returns the position of the encoder as a Double
		val encoder = Calcified.controlHub.getTicksEncoder(0)
		// separate from the motor direction
		encoder.direction = Direction.FORWARD
		// the position
		encoder.position

		// the position can be set, this is done software side
		encoder.position = 100.0
		// whereas the reset method does the hardware instruction
		encoder.reset()

		// velocity is taken over a period of the measurementWindow (measured in seconds), which defaults to 20 ms
		// this is because velocity from encoders can be very noisy
		encoder.measurementWindow = 0.02
		encoder.velocity

		// raw velocity is not measured over a buffer / window
		encoder.rawVelocity

		// acceleration is the same
		encoder.acceleration
		encoder.rawAcceleration

		// encoders are part of the unit system
		// encoders take the form of EnhancedNumberSuppliers
		// for instance, I can form a new encoder output, that passes the output of the first encoder through a function
		val modifiedEncoder = encoder.applyModifier { x -> x / 2 }
		modifiedEncoder.position // half of encoder.position!
		// EnhancedSuppliers are all immutable, which means that modifying methods actually produce a new EnhancedSupplier, independent of the previous one
		// and can be used to easily form conditional binds
		// more on this later!

		// encoders can be used directly as other unit systems, such as Angles, and Distances
		val absoluteEncoder = Calcified.controlHub.getAngleEncoder(1, Wrapping.WRAPPING, 28.0)
		val distanceEncoder = Calcified.controlHub.getDistanceEncoder(2, DistanceUnits.MILLIMETER, 10.0)
		// these work the same as above, but everything is measured as the appropriate reified unit

		val combinedEncoder = distanceEncoder.merge(encoder::velocity) { distance, velocity -> distance * velocity }

		// Angles
		val angle = absoluteEncoder.position
		// angles can be either wrapping or linear
		// for an encoder that is set up to be wrapping, it still produces linear angles for velocity and acceleration
		// wrapping means that an angle exists in the domain of [0, 1] rotations
		// linear can exist outside of this range
		// linear can easily be converted down

		// the find error method also exists for units
		// for angles, this method is important, as if the target is wrapping, then the output will be in the domain [-0.5, 0.5] rotations
		// and will be corrected to find the shortest distance
		// this finds the shortest distance from angle to 0
		angle.findError(Angle(AngleUnits.RADIAN, Wrapping.WRAPPING, 0.0))

		// angles have two predefined units, radians and degrees

		// angles also have trig helpers
		angle.sin
		angle.cos
		angle.tan

		// Distance
		val distance = distanceEncoder.position
		// distances have 4 predefined units:
		// meter, millimeter, foot, inch

		// It is likely that you might want to define your own distance unit for the size of your tile
		// which would be helpful while developing auto, as you can quickly modify the size of a tile when you go to competition

		// units can also be multiplied and divided by doubles
		distance / 2.0
		// put to the power of
		distance.pow(2)
		distance.sqrt()
		// or absolute valued
		distance.abs()
		// or coerced
		distance.coerceAtLeast(Distance.NEGATIVE_INFINITY)

		//
		// Servos
		//
		// Servos are fairly simple
		val servo = Calcified.controlHub.getServo(0)
		// same as the sdk
		servo.position = 0.0
		servo.cachingTolerance = 0.01 // tolerance on setting the position, behaves the same as setting motor power
		servo.enabled = true // enables / disables the port, TODO this may cause both 'linked' servo ports to become disabled, this behaviour has not yet been tested
		servo.direction = Direction.FORWARD // same as the sdk, reverse inverts 0 and 1
		servo.pwmRange = PwmControl.PwmRange.defaultRange // directly exposes the pwmRange, which can be used to easily change the pwm information, in order to make use of servos that use a different range

		// CR Servos have all the same things, but uses power instead
		val crServo = Calcified.controlHub.getContinuousServo(1)
		crServo.power = 0.0
		crServo.cachingTolerance = 0.02
		crServo.enabled = true // enables / disables the port, TODO this may cause both 'linked' servo ports to become disabled, this behaviour has not yet been tested
		crServo.direction = Direction.FORWARD
		crServo.pwmRange = PwmControl.PwmRange.defaultRange // directly exposes the pwmRange, which can be used to easily change the pwm information, in order to make use of servos that use a different range

		// crServos can be used in a motor controller group with motors
		val motorControllerGroup = MotorControllerGroup(motor0, motor1, motor2, crServo)
		motorControllerGroup.power = 1.0

		//
		// IMUs
		//
		// the imu can be obtained with full defaults
		val imu = Calcified.controlHub.getIMU()
		Calcified.controlHub.getIMU(
				0, // port defaults to 0
				LynxModuleImuType.BHI260, // if you don't supply this value, it is automatically detected, which is probably for the best
				fromImuOrientationOnRobot( // there are lots of ways to generate a starting orientation for the imu!
						RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.FORWARD, RevHubOrientationOnRobot.UsbFacingDirection.UP)
				)
		)
		// Dairy uses its own orientation system, but provides high compatibility with the sdk classes for orientation
		// the following should all be equivalent
		val orientation = AngleBasedRobotOrientation(
				Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90.0),
				Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90.0),
				Angle(AngleUnits.DEGREE, Wrapping.WRAPPING, 90.0),
		)
		val orientationFromHubOrientation = fromImuOrientationOnRobot(RevHubOrientationOnRobot(RevHubOrientationOnRobot.LogoFacingDirection.FORWARD, RevHubOrientationOnRobot.UsbFacingDirection.UP))
		val orientationFromYawPitchRollAngles = fromYawPitchRollAngles(YawPitchRollAngles(AngleUnit.DEGREES, 90.0, 90.0, 90.0, 0))

		// and the opposite can also be done
		orientation.toYawPitchRoll().getYaw(AngleUnit.DEGREES)

		// but the Dairy orientation system provides support through the angle units we looked at previously
		// there are sugar shortcuts for heading (zRot)
		imu.heading
		imu.orientation.xRot
		imu.orientation.yRot
		imu.orientation.zRot
		// the enhanced supplier implementations for the axes
		imu.headingSupplier
		imu.xRotSupplier
		imu.yRotSupplier
		imu.zRotSupplier

		// nice and easy!
		// also, the heading and orientation be set
		imu.heading = angle
		imu.orientation = orientationFromYawPitchRollAngles

		//
		// Analog Sensors
		//
		// another enhanced supplier!
		// this one is just doubles
		val aInput = Calcified.controlHub.getAnalogInput(0)
		// nothing new...
		aInput.position
		aInput.velocity
		aInput.rawVelocity
		// ...

		//
		// Digital Sensors
		//
		// digital inputs are EnhancedBooleanSuppliers
		// they are different to the EnhancedNumberSuppliers we have seen so far
		val dInput = Calcified.controlHub.getDigitalInput(0)
		dInput.state // true / false
		// rising and falling edge detection
		dInput.onTrue
		dInput.onFalse

		// debouncing can be applied
		// remember that these are immutable, so each new EnhancedBooleanSupplier is independent from the others
		dInput.debounce(0.05)
		dInput.debounce(0.05, 0.0)
		dInput.debounceRisingEdge(0.05)
		dInput.debounceFallingEdge(0.05)

		// the suppliers can be combined
		dInput and { true }
		dInput or { false }
		dInput xor { false }

		// or inverted!
		dInput.not()

		// dOutputs are pretty boring, they just accept a variable
		val dOutput = Calcified.controlHub.getDigitalOutput(1)
		dOutput.accept(true)

		//
		// Conditionals
		//
		// the conditionals system makes it easy to build EnhancedBooleanSuppliers from EnhancedNumberSuppliers
		val simpleBinding = encoder.conditionalBindPosition()
				.greaterThan(0.0)
				.lessThan(100.0)
				.bind()

		simpleBinding.state
		simpleBinding.onTrue

		// this system is very intuitive, and binding works best if you go in order of smallest to largest, and it can be very complex
		val complexBinding = encoder.conditionalBindVelocity()
				.greaterThanEqualTo(-100.0)
				.lessThan(-10.0)
				.greaterThan(200.0)
				.bind()
		// this more complex binding will return true if the encoder has a velocity in the following domains: [-100, -10), (200, infinity)

		// there are conditional binding builders for all components of motion supplied by an EnhancedNumberSupplier
		encoder.conditionalBindPosition()
		encoder.conditionalBindVelocity()
		encoder.conditionalBindVelocityRaw()
		encoder.conditionalBindAcceleration()
		encoder.conditionalBindAccelerationRaw()

		// but you can also make your own
		val startTime = System.nanoTime() / 1e9
		val customTimer = Conditional { (System.nanoTime() / 1e9) - startTime }

		// conditionals can also be reused!
		val after90 = customTimer.greaterThan(90.0).bind()
		val pre30 = customTimer.lessThan(30.0).bind()

		//
		// ComplexControllers
		//
		// ComplexControllers are the Calcified alternative to run modes, allowing for powerful, extensible control loops
		// We start with a compiler, this one is built around processing Doubles, but there are also options for Units
		// controller compilers are immutable, so keep that in mind
		val doubleCompiler = DoubleControllerCompiler()
				.add(motorControllerGroup) // we'll control the motors of the motor controller group
				.withSupplier(encoder) // we need to attach a supplier to use for input and / or feedback
				.append(DoublePComponent(0.1))
				.append(DoubleDComponent(0.0005))
				.append(DoubleIComponent(-0.00003, -0.1, 0.1))

		// cool!, we just built a PID controller
		val doubleController = doubleCompiler.compile(1000.0, MotionComponents.POSITION, 10.5)
		// this will automatically update in the background
		// and we can update its information
		doubleController.target = 100.0
		doubleController.toleranceEpsilon = 15.0
		// the motion component determines what information is given to the PID algorithms
		doubleController.motionComponent = MotionComponents.VELOCITY
		// before, they ran off the position of the encoder, now they run off the velocity
		// or check out how its going
		doubleController.finished() // if within acceptable error of the target, determined using the toleranceEpsilon
		doubleController.finished(100.0) // or we can supply our own temporarily

		// but we can do more!
		doubleCompiler
				// its simple to write your own lambda / class implementation of the calculation component
				// due to the type system, it is necessary for implementations to do the summation process themselves
				// so remember to return accumulation + the output you found
				.append { accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double ->
					accumulation + (error / 2) * currentState
				}

		// there are also unit based controller systems

		// note that its also easy to pipe the output of one controller to another!
		// this controller just produces an output, for another one
		val veloController = UnitControllerCompiler<DistanceUnit, Distance>()
				.withSupplier(distanceEncoder)
				.append(UnitPComponent(0.5))
				.append(UnitDComponent(0.5))
				.append(UnitIComponent(0.5))
				.compile(Distance(DistanceUnits.METER, 0.2), MotionComponents.POSITION, Distance(DistanceUnits.MILLIMETER, 10.0))

		UnitControllerCompiler<DistanceUnit, Distance>()
				.set(motorControllerGroup)
				.withSupplier(distanceEncoder)
				.append(UnitPComponent(0.5))
				.append(UnitDComponent(0.5))
				.append(UnitIComponent(0.5))
				// we pipe the output of the velocity controller, to the target of this one
				.compile(veloController::output, MotionComponents.VELOCITY, Distance(DistanceUnits.MILLIMETER, 1.0))

		// now we have a PID on position controller, that produces a target velocity output, and all we need to do to update both systems, is change the position target
		veloController.target = Distance(DistanceUnits.MILLIMETER) // 0

		// you might want to stop a controller from running for a bit, e.g. if you need to switch to manual control mode
		doubleController.enabled = false
		// for example, this stops double controller from updating in the background

		// And that's all!
		// If you're interested in Gamepad support in a similar vein, checkout the Pasteurized overview
		// It explains some of the topics around the EnhancedSupplier family as well, so you may find some of what it explains, helps you to understand this
	}

	override fun loop() {
	}
}