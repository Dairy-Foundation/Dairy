package org.firstinspires.ftc.teamcode.examples.controller;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.HashMap;
import java.util.function.Supplier;

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation;
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.BinaryBranch;
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.Branch;
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.DoubleComponent;
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.UnitComponent;
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleController;
import dev.frozenmilk.dairy.core.util.controller.implementation.PoseController;
import dev.frozenmilk.dairy.core.util.controller.implementation.UnitController;
import dev.frozenmilk.dairy.core.util.controller.implementation.VectorController;
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents;
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier;
import dev.frozenmilk.util.units.distance.Distance;
import dev.frozenmilk.util.units.distance.DistanceUnit;
import dev.frozenmilk.util.units.distance.DistanceUnits;

public class JavaOverview extends OpMode {
	private Distance targetDistance = new Distance(DistanceUnits.MILLIMETER, 10.0);
	@Override
	public void init() {
		DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "m");
		EnhancedDoubleSupplier doubleEncoder = new EnhancedDoubleSupplier(() -> (double) motor.getCurrentPosition());
		//
		// Controllers
		//
		// ComplexControllers are the Dairy alternative to run modes, allowing for powerful, extensible control loops
		
		DoubleController doubleController = new DoubleController(
				0.0, // we can change our target again later
				doubleEncoder, // we'll use the motor's encoder for feedback
				MotionComponents.POSITION, // we're going to work with position
				0.001, // when we check finished, this is our standard allowable error
				motor::setPower, // when this controller updates, this callback will be run
				// then we build up the calculation:
				new DoubleComponent.P(0.1) // first P
						.plus(new DoubleComponent.I(-0.00003, -0.1, 0.1)) // then I
						.plus(new DoubleComponent.D(0.0005)) // then D
		);
		
		// cool!, we just built a PID controller
		// this will automatically update in the background
		// and we can update its information
		doubleController.setTarget(100.0);
		doubleController.setToleranceEpsilon(15.0);
		// the motion component determines what information is given to the PID algorithms
		doubleController.setMotionComponent(MotionComponents.VELOCITY);
		// before, they ran off the position of the encoder, now they run off the velocity
		// or check out how its going
		doubleController.finished(); // if within acceptable error of the target, determined using the toleranceEpsilon
		doubleController.finished(100.0); // or we can supply our own temporarily
		
		// but we can do more!
		ControllerCalculation<Double> calculation =
				// its simple to write your own lambda / class implementation of the controller calculation
				// due to the type system, it is necessary for implementations to do the summation process themselves
				// so remember to return accumulation + the output you found
				((ControllerCalculation<Double>) (Double accumulation, Double currentState, Double target, Double error, double deltaTime) -> {
					return accumulation + (error / 2) * currentState;
				}).plus(new DoubleComponent.P(0.05)); // and we can easily compose it with others!
		
		// there are also unit based controller systems
		
		// note that its also easy to pipe the output of one controller to another!
		// this controller just produces an output, for another one
		EnhancedUnitSupplier<DistanceUnit, Distance> distanceEncoder = new EnhancedUnitSupplier<>(() -> new Distance(DistanceUnits.MILLIMETER, doubleEncoder.getPosition()));
		UnitController<DistanceUnit, Distance> positionController = new UnitController<>(
				(Supplier<? extends Distance>) () -> targetDistance, 	// instead of a static target, we can use a dynamic one, now,
				// the target will update when we change the local variable
				distanceEncoder,
				MotionComponents.POSITION,
				new Distance(DistanceUnits.INCH, 0.2),
				new UnitComponent.P<DistanceUnit, Distance>(0.5)
						.plus(new BinaryBranch.Target<Distance>((target) -> target.greaterThan(new Distance(DistanceUnits.FOOT, 1))) // there are also branching utilities in controllers
								.forceEvalTrue(new UnitComponent.D<DistanceUnit, Distance>(2.0)))
						 // this be added if the target is larger than 1 foot
				// we'll look more at branching utilities shortly
				// also, notice that we didn't add an outputConsumer? this controller is just going to do the calculations for it, it won't update anything
		);
		
		UnitController<DistanceUnit, Distance> veloController = new UnitController<DistanceUnit, Distance>(
				positionController::getPosition, // we're going to use the output of the position PID we just put together, to drive this velocity controller
				distanceEncoder, // we're using the same distance encoder
				MotionComponents.VELOCITY, // this time we're working with velocity
				new Distance(DistanceUnits.INCH, 0.2),
				distance -> motor.setPower(distance.intoCommon().getValue()), // we're using units, so we need to do a little adapting here
				new UnitComponent.P<DistanceUnit, Distance>(0.5)
		);
		
		// now we have a PID on position controller, that produces a target velocity output, and all we need to do to update both systems, is change the position target
		targetDistance = new Distance(); // 0
		
		// you might want to stop a controller from running for a bit, e.g. if you need to switch to manual control mode
		doubleController.setEnabled(false);
		
		// for example, this stops double controller from updating in the background
		
		//
		// Branching
		//
		
		// binary branches get all the same arguments as ControllerCalculations, but return a boolean instead
		BinaryBranch<Double> binaryBranch = new BinaryBranch<>((accumulation, currentState, target, error, deltaTime) -> {
			return deltaTime > 0.2;
		});
		
		// BinaryBranch has helpful subclasses for working with a single component:
		BinaryBranch<Double> simpleBranch = new BinaryBranch.Target<>((target) -> target > 0.0); // we'll use this binary branch for the following examples.
		
		// once we have a binary branch, we can add Components to be conditionally added to the composed calculation
		simpleBranch.evalTrue(new DoubleComponent.P(0.0)); // gets added if the binaryBranch evaluates to true
		simpleBranch.evalFalse(new DoubleComponent.P(0.0)); // gets added if the binaryBranch evaluates to false
		simpleBranch.eval(
				new DoubleComponent.P(0.0), // gets added if the binaryBranch evaluates to true
				new DoubleComponent.P(0.0) // gets added if the binaryBranch evaluates to false
		);
		
		// each of the forceEval functions will run its component's evaluation function regardless of its used,
		// but will ignore the result,
		
		// this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
		// e. g. D terms where previous error and delta time may be differently timestamped
		simpleBranch.forceEvalTrue(new DoubleComponent.P(0.0));
		simpleBranch.forceEvalFalse(new DoubleComponent.P(0.0));
		simpleBranch.forceEval(
				new DoubleComponent.P(0.0), // evalTrue
				new DoubleComponent.P(0.0) // evalFalse
		);
		
		// non-binary branches use a map
		Branch<Double, Branches> branch = new Branch<>((accumulation, currentState, target, error, deltaTime) -> {
			if (currentState > 0) return Branches.FIRST;
			else if (currentState < 0) return Branches.SECOND;
			else return Branches.THIRD;
		});
		
		// Branch also has the same helper subclasses
		// this is the same as above
		new Branch.State<Double, Branches>((currentState) -> {
			if (currentState > 0) return Branches.FIRST;
			else if (currentState < 0) return Branches.SECOND;
			else return Branches.THIRD;
		});
		
		// we need a map for branch to look up in
		// this map is missing Branches.THIRD, which means that branch will be a no-op or default
		HashMap<Branches, ControllerCalculation<Double>> condMap = new HashMap<Branches, ControllerCalculation<Double>>() {{
			put(Branches.FIRST, new DoubleComponent.P(0.0));
			put(Branches.SECOND, new DoubleComponent.P(0.0));
		}};
		
		branch.map(condMap); // runs our condition, and then looks up the resulting component in the map
		branch.mapOrElse(condMap, new DoubleComponent.P(0.0)); // runs the default if there would be a no-op
		
		branch.forceMap(condMap); // like the force variations of BinaryBranch, runs all components regardless of which is used
		branch.forceMapOrElse(condMap, new DoubleComponent.P(0.0)); // runs the default if there would be a no-op, default is also force evaluated
		
		// The joys of function composition allow you to do pretty much anything you want with this system,
		// and run on any hardware api platform.
		
		// In the future, Dairy should provide more default components to use, and types to use
		// along with tools for more complicated controller setup
		
		// supported in addition to Doubles and Units:
		VectorController vectorController;
		PoseController poseController;
	}
	
	private enum Branches {
		FIRST,
		SECOND,
		THIRD
	}
	
	@Override
	public void loop() {
	}
}
