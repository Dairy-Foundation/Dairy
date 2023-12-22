package org.firstinspires.ftc.teamcode.examples.calcified;

import static dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMUKt.fromImuOrientationOnRobot;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.LynxModuleImuType;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import dev.frozenmilk.dairy.calcified.Calcified;
import dev.frozenmilk.dairy.calcified.gamepad.EnhancedBooleanSupplier;
import dev.frozenmilk.dairy.calcified.gamepad.EnhancedNumberSupplier;
import dev.frozenmilk.dairy.calcified.gamepad.EnhancedNumberSupplierKt;
import dev.frozenmilk.dairy.calcified.hardware.controller.LambdaController;
import dev.frozenmilk.dairy.calcified.hardware.controller.LambdaControllerKt;
import dev.frozenmilk.dairy.calcified.hardware.controller.PController;
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedEncoder;
import dev.frozenmilk.dairy.calcified.hardware.motor.CalcifiedMotor;
import dev.frozenmilk.dairy.calcified.hardware.motor.DegreesEncoder;
import dev.frozenmilk.dairy.calcified.hardware.motor.Direction;
import dev.frozenmilk.dairy.calcified.hardware.motor.RadiansEncoder;
import dev.frozenmilk.dairy.calcified.hardware.motor.ZeroPowerBehaviour;
import dev.frozenmilk.dairy.calcified.hardware.sensor.AnalogInput;
import dev.frozenmilk.dairy.calcified.hardware.sensor.CalcifiedIMU;
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalInput;
import dev.frozenmilk.dairy.calcified.hardware.sensor.DigitalOutput;
import dev.frozenmilk.dairy.calcified.hardware.servo.CalcifiedContinuousServo;
import dev.frozenmilk.dairy.calcified.hardware.servo.CalcifiedServo;
import dev.frozenmilk.dairy.core.DairyCore;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.OpModeLazyCell;
import dev.frozenmilk.util.angle.Angle;
import dev.frozenmilk.util.angle.AngleDegrees;
import dev.frozenmilk.util.angle.AngleRadians;
import dev.frozenmilk.util.cell.Cell;
import dev.frozenmilk.util.orientation.AngleBasedRobotOrientation;
import dev.frozenmilk.util.profile.ProfileConstraints;
import dev.frozenmilk.util.profile.ProfileStateComponent;

@TeleOp
@Calcified.Attach( // attaches the Calcified feature
		automatedCacheHandling = true, // these are settings for the feature that we can set
		crossPollinate = true // setting both to true is the default, but if you're a more advanced user you may want to make use of these
)
// @DairyCore
// can also be used to activate all dairy library features, but doesn't allow settings,
// also, if @DairyCore is present it will clash with the @Calcified.Attach annotation
public class JavaOverview extends OpMode {
	public JavaOverview() {
		// this ensures that Calcified is attached,
		// if it failed for some reason, then it will spit out a helpful error describing why
		// what you asked for wasn't successfully attached
		
		// if this line isn't here, the first time you run an OpMode with calcified in it it might crash,
		// and then work after that, due to the way classes are loaded in java,
		// so this line is advised even if you know that everything should be fine
		FeatureRegistrar.checkFeatures(this, Calcified.INSTANCE);
	}
	
	// fields which are used as demo, ignore these for the moment and come back to them when later comments refer to them
	private CalcifiedMotor motor1;
	private final Cell<CalcifiedMotor> motor2 = new OpModeLazyCell<>(() -> {
		CalcifiedMotor motor = Calcified.getControlHub().getMotor((byte) 2);
		motor.setDirection(Direction.REVERSE);
		motor.setCachingTolerance(0.01);
		motor.setZeroPowerBehaviour(ZeroPowerBehaviour.BRAKE);
		return motor;
	});
	
	@Override
	public void init() {
		// Calcified provides access to the control and expansion hubs like so:
		Calcified.getControlHub();
		Calcified.getExpansionHub();
		// if one of them isn't plugged in electronically, accessing it will throw an error
		
		// from now on, the control hub will be used to show accessing hardware objects, the two hubs are functionally equivalent
		
		// motors and similar hardware objects can be written as lateinit var fields as usual, and then inited in this init block
		// for example, motor0
		motor1 = Calcified.getControlHub().getMotor((byte) 0);
		motor1.setZeroPowerBehaviour(ZeroPowerBehaviour.BRAKE);
		
		// if you are going to do more complex set up operations, it can be nice to instead bundle your initialisation into an OpModeLazyCell, like motor1
		// even if i don't access motor1 here, its contents get evaluated, which prevents issues from relying on lazy initialisation, and allows more complex
		// initialisation sequences to bundled onto the motor itself, rather than clogging up the init block
		// this allows a whole bunch of declarative code to be set up in these cells, especially if you're using calcified motor controllers,
		// which use dairy core to automatically update themselves, like RUN_TO_POSITION
		
		// note, cells work best in kotlin, where they can be used via delegation, and act exactly like their contents
		// in java this can't be done, so instead the contents of the cell need to be retrieved to be acted upon, like so:
		CalcifiedMotor retrieved = motor2.get();
		
		// Now that we've reviewed initialisation, we'll review the hardware objects in calcified
		//
		// MOTORS:
		//
		CalcifiedMotor motor = Calcified.getControlHub().getMotor((byte) 0);
		motor.setZeroPowerBehaviour(ZeroPowerBehaviour.BRAKE); // pretty self explanatory, the same as the option in the sdk
		motor.setDirection(Direction.FORWARD); // once again
		// determines the level of difference between one value and the next before a write is performed
		motor.setCachingTolerance(0.005); // you might want to lower it for more fine grained control, or raise it for less
		// the lower it is, the slower your loops will be, and visa versa
		
		motor.setEnabled(true); // if set to false, will stop the motor from moving, matches the sdk
		motor.setPower(1.0); // same as the sdk, does some more checks first to see if it should write first though
		motor.getCurrent(CurrentUnit.AMPS); // same as the sdk
		// and thats it for motors, you might be thinking, what happened to encoders?, or run modes?
		// calcified does some major reorganising of ideas here, but offers more powerful alternatives
		
		//
		// ENCODERS:
		//
		// encoders come in several flavours
		// the ticks encoder is the standard variant
		CalcifiedEncoder<Integer> encoder = Calcified.getControlHub().getTicksEncoder((byte) 0);
		// additionally, the radians and degrees encoders wrap into absolute degree measurements
		RadiansEncoder radEncoder = Calcified.getControlHub().getRadiansEncoder((byte) 1, 300.0); // the ticks per revolution let the encoder know how to derive the number of ticks in a radian
		DegreesEncoder degEncoder = Calcified.getControlHub().getDegreesEncoder((byte) 2, 300.0); // the ticks per revolution let the encoder know how to derive the number of ticks in a degree
		// don't worry too much about which of these you want to use, most most of the time you'll probably want the RadiansEncoder
		// but their outputs can be easily converted
		AngleRadians radians = radEncoder.getPosition();
		AngleDegrees degrees = radians.intoDegrees();
		
		// encoders offer two big pieces of information pre-built into them
		// the position supplier
		encoder.getPositionSupplier();
		// the position itself
		encoder.getPosition(); // these are the same
		encoder.getPositionSupplier().get();
		// the position can also be set
		encoder.setPosition(100);
		// the encoder will now measure as though it really was at position 100 there
		
		// and the velocity supplier
		encoder.getVelocitySupplier();
		// the velocity itself
		encoder.getVelocity(); // these are the same
		encoder.getVelocitySupplier().get();
		
		// both of which are pre-wrapped with the capability to automatically calculate error
		encoder.getPositionSupplier().findError(100); // how far away is the encoder from 100 ticks?
		encoder.getVelocitySupplier().findError(54.3); // how far away is the encoder from a velocity of 54.3 ticks / second?
		
		// for other types of encoders, these properties stay true
		degEncoder.getPositionSupplier().findError(new AngleDegrees(180)); // how far away is the encoder from 180 degrees?
		degEncoder.getVelocitySupplier().findError(1000.0); // how far away is the encoder from 1000 degrees per second?
		
		// but these features probably won't come into handy too often for you
		// instead they allow encoders to interface super nicely with the motor controller interfaces, which we'll review later
		
		//
		// Servos:
		//
		// servos work much like servos in the sdk:
		CalcifiedServo servo = Calcified.getControlHub().getServo((byte) 0);
		servo.setDirection(Direction.FORWARD);
		servo.setEnabled(true);
		servo.setCachingTolerance(0.001); // same as the motor, but for changes in position
		servo.setPwmRange(PwmControl.PwmRange.defaultRange);  // this version supplies immediate access the pwm range object, used to control the servo positions
		servo.setPosition(0.0); // similar to the motor, performs similarly to the set position on a servo from the base sdk, but does some more checks before it writes
		
		// everything on the crservo performs the same as it does on the motor and / or servo
		CalcifiedContinuousServo crServo = Calcified.getControlHub().getContinuousServo((byte) 1);
		crServo.setDirection(Direction.FORWARD);
		crServo.setEnabled(true);
		crServo.setCachingTolerance(0.005);
		crServo.setPwmRange(PwmControl.PwmRange.defaultRange);
		crServo.setPower(0.0);
		
		//
		// IMUs:
		//
		// there are several ways to initialise an imu, supporting both old ways of initialising IMUs and new ones
		// remember, the IMU built into the REV Control / Expansion Hubs are in port 0
		CalcifiedIMU imu_BHI260 = Calcified.getControlHub().getIMU_BHI260((byte) 0);
		CalcifiedIMU imu_BNO055 = Calcified.getControlHub().getIMU_BNO055((byte) 1, fromImuOrientationOnRobot(new RevHubOrientationOnRobot(
				RevHubOrientationOnRobot.LogoFacingDirection.UP,
				RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
		)));
		CalcifiedIMU imu = Calcified.getControlHub().getIMU((byte) 2, LynxModuleImuType.BNO055, new AngleBasedRobotOrientation(new AngleDegrees(50.0), new AngleDegrees(-40.7), new AngleDegrees()));
		
		// the imu supports the sdk's default way of doing angles
		imu.getYawPitchRollAngles();
		
		imu.getHeading(); // the heading of the robot
		// the angles from the robot orientation
		imu.getOrientation().getXRot();
		imu.getOrientation().getYRot();
		imu.getOrientation().getZRot(); // this is the same as the heading
		
		// like the encoders, the imu supports some complex suppliers
		imu.getHeadingSupplier().get(); // the heading of the robot, same as imu.heading
		imu.getHeadingSupplier().findError(new AngleDegrees(50.0)); // how far away is the heading of the robot from 50 degrees?
		
		imu.getXRotSupplier();
		imu.getYRotSupplier();
		imu.getZRotSupplier(); // the same the as the heading supplier
		
		// setting the imu's orientation does so for all the axes
		imu.setOrientation(new AngleBasedRobotOrientation(new AngleDegrees(90.0), new AngleDegrees(), new AngleDegrees()));
		
		// the above is equivalent to the below, due to how frequently teams only use the heading
		imu.setHeading(new AngleDegrees(90.0));
		
		// velocities can also be obtained
		imu.getHeadingVelocity();
		imu.getXRotVelocity();
		imu.getYRotVelocity();
		imu.getZRotVelocity(); // the same as heading velocity
		
		// and their complex supplier forms
		// which can provide error and value
		imu.getHeadingVelocitySupplier();
		imu.getXRotVelocitySupplier();
		imu.getYRotVelocitySupplier();
		imu.getZRotVelocitySupplier(); // same as the headingVelocitySupplier
		
		//
		// Digital Channels
		//
		DigitalInput digitalInput = Calcified.getControlHub().getDigitalInput((byte) 0);
		digitalInput.get(); // the state of the sensor
		digitalInput.getEnhanced(); // an enhanced version of the sensor supplier
		// the enhanced version provides several improvements to a normal boolean supplier in the context of writing robot code
		// these features are also provided on the gamepad buttons, and so they'll be covered in depth in that section
		digitalInput.getEnhanced().getWhenTrue();
		// digital outputs are super simple
		DigitalOutput digitalOutput = Calcified.getControlHub().getDigitalOutput((byte) 1);
		digitalOutput.accept(true);
		
		//
		// Analog Sensors
		//
		// analog inputs are the same as digital inputs
		AnalogInput analogInput = Calcified.getControlHub().getAnalogInput((byte) 0);
		analogInput.get();
		analogInput.getEnhanced();
		// the rest of the hardware objects are being worked on
		
		//
		// Motor Controllers
		//
		// Calcified offers powerful motor controllers instead of the run to position, and run with encoders run modes
		LambdaController<Integer> controller = new LambdaController<>(0)
				// attaches a mix of motors and cr servos to be updated by the controller
				.addMotors(motor, motor1, motor2.get(), crServo)
				// says to use the position supplier on the encoder for each of the following
				.withErrorSupplier(encoder.getPositionSupplier())
				// adds a P term
				.appendPController(0.1)
				// adds an I term
				.appendIController(0.00001, 0.0, 0.2)
				// adds a D term
				.appendDController(0.005);
		
		// causes this controller to automatically have .update() called on it, do this after finishing building the whole controller,
		// and turn it off again before making further changes
		controller.setAutoUpdate(true);
		// this method updates the controller by hand, if auto update is off, this needs to be called for the controller to run
		controller.update();
		// the target of the controller can be changed simply:
		controller.setTarget(1000);
		
		LambdaController<Angle> armController = new LambdaController<Angle>(new AngleDegrees(0.0))
				// .intoGeneric() converts any supplier of a particular angle type into a more generic supplier
				.withPositionSupplier(degEncoder::getPosition);
				// an arm FF controller can only be appended to a controller that takes in an Angle as a target
				// and runs the angle the supplier through the cos function, which means if you set up your encoder correctly,
				// this will provide full power when the arm is out flat, and none when its vertical
		armController = LambdaControllerKt.appendArmFFController(armController, 0.1);
		
		// note, android studio seems to get very confused about this block of code, bc of the full appendPController line (4 below this line), without it, things are fine
		LambdaController<Double> otherController = new LambdaController<>(0.0)
				// controllers can also be appended like so:
				.appendPController(new PController(encoder.getVelocitySupplier(), 0.7))
				.appendDController(0.008)
				// this is the same as:
				.withErrorSupplier(encoder.getVelocitySupplier())
				.appendPController(0.7)
				.appendDController(0.008);
		// and will cause the error supplier passed in to be inferred for further arguments
		
		LambdaController<Integer> profiledController = new LambdaController<Integer>(0)
				.withPositionSupplier(encoder.getPositionSupplier());
		profiledController = LambdaControllerKt.appendProfiledController(profiledController, new ProfileConstraints(100.0, 10.0), ProfileStateComponent.Position);
		
		// motion profiles can also be used in controllers!
		
		//
		// Gamepads:
		//
		// Calcified also offers advanced versions of the Gamepads
		Calcified.getGamepad1();
		Calcified.getGamepad2();
		
		// buttons on the gamepads are represented by EnhancedBooleanSuppliers
		// which we also saw when looking at digital and analog inputs
		EnhancedBooleanSupplier enhancedBooleanSupplier = Calcified.getGamepad1().getA();
		
		enhancedBooleanSupplier.get(); // current state
		enhancedBooleanSupplier.getWhenTrue(); // a rising edge detector
		enhancedBooleanSupplier.getWhenFalse(); // a falling edge detector
		enhancedBooleanSupplier.getToggleTrue(); // a toggle that gets changed whenever a rising edge is detected
		enhancedBooleanSupplier.getToggleFalse(); // a toggle that gets changed whenever a falling edge is detected
				
		// EnhancedBooleanSuppliers are immutable by default, so you can pull them out of the gamepad, do one-off modifications to them, and then store and use them again later
		
		// debouncing can be applied independently to both the rising and falling edge
		// note that each of these operations does not modify the original supplier, attached to gamepad1.a
		enhancedBooleanSupplier = enhancedBooleanSupplier.debounce(0.1);
		enhancedBooleanSupplier = enhancedBooleanSupplier.debounce(0.1, 0.0);
		enhancedBooleanSupplier = enhancedBooleanSupplier.debounceFallingEdge(0.1);
		enhancedBooleanSupplier = enhancedBooleanSupplier.debounceRisingEdge(0.1);
		
		// if we do not reassign the new EnhancedBooleanSupplier to the variable, or store it in a different variable it will be lost
		
		// suppliers can also be combined:
		enhancedBooleanSupplier = enhancedBooleanSupplier.and(() -> encoder.getPosition() > 5);
		enhancedBooleanSupplier = enhancedBooleanSupplier.or(() -> encoder.getVelocity() < 100.0);
		
		// this works is all kinds of ways!
		EnhancedBooleanSupplier twoButtons = Calcified.getGamepad1().getA().and(Calcified.getGamepad1().getB());
		
		// you can also reassign the buttons on the gamepads themselves, if you wish to make a change more global
		Calcified.getGamepad2().setA(Calcified.getGamepad1().getA().or(Calcified.getGamepad2().getA()));
		// now either the driver or the operator can trigger this condition!
		
		// note: the calcified gamepads have remaps for all gamepad buttons and inputs, the inputs that are shared across the different gamepad types
		// but share a name (i.e. cross on a ps4 controller and a on a logitech or x-box controller) are linked together on the calcified gamepad
		
		// sticks and triggers are represented via EnhancedNumberSuppliers
		EnhancedNumberSupplier<Double> enhancedNumberSupplier = Calcified.getGamepad1().getLeftStickY();
		// the value of the stick
		enhancedNumberSupplier.get();
		
		// deadzones can be applied, much like the EnhancedBooleanSupplier, these operations are non-mutating
		enhancedNumberSupplier = enhancedNumberSupplier.applyDeadzone(0.1); // becomes -0.1, 0.1
		enhancedNumberSupplier = enhancedNumberSupplier.applyDeadzone(-0.1, 0.2);
		enhancedNumberSupplier = enhancedNumberSupplier.applyUpperDeadzone(-0.1);
		enhancedNumberSupplier = enhancedNumberSupplier.applyLowerDeadzone(0.1);
		
		// EnhancedNumberSuppliers also interact well with building complex EnhancedBooleanSuppliers from ranges
		EnhancedBooleanSupplier rangeBasedCondition = enhancedNumberSupplier.conditionalBind()
				.greaterThan(-0.5)
				.lessThan(0.5)
				.bind();
		
		// this system is fairly intuitive, and works best if you list numbers from smallest to largest,
		// or in pairs e.g.:
		EnhancedBooleanSupplier complexRangeBasedCondition = enhancedNumberSupplier.conditionalBind()
				.greaterThan(0.0)
				.lessThan(10.0)
				.greaterThanEqualTo(1.0)
				.lessThanEqualTo(1000.0)
				.bind();
		
		// forms two acceptable ranges,
		// but obviously this could be simplified
		// imagine you were reading out each condition and then drawing it on the number line.
		// if it can form a closed range with the last condition you listed, it will!
		
		// for 90% of use cases it is just best to list numbers from smallest to largest, the rest will work itself out
		
		// this process works for all Supplier<Double>s and so can be used on things like encoders:
		
		EnhancedBooleanSupplier encoderBasedCondition = EnhancedNumberSupplierKt.conditionalBind(encoder.getVelocitySupplier())
				.greaterThanEqualTo(100.0)
				.lessThanEqualTo(250.0)
				.bind();
		
		// remember, its best to run these operations once at the start of the op mode, and store them for later,
		// as they are reasonably expensive to remake every loop
		// but checking
		encoderBasedCondition.getWhenTrue();
		// will run all the correct checks against the encoder position whenever you call it, but only if you call it
		
		// Hopefully this has been a helpful overview of how to use Calcified, hosted on DairyCore
		// See the other examples for better usage examples of these features (if there are no other examples uhh, this should be fine tbh, and todo)
		
	}
	
	@Override
	public void loop() {
	
	}
}

