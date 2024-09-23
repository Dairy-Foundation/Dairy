package org.firstinspires.ftc.teamcode.examples.controller;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.EnumMap;

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation;
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent;
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponents;
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.BinaryBranch;
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.Branch;
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.DoubleComponent;
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.UnitComponent;
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleController;
import dev.frozenmilk.dairy.core.util.controller.implementation.DistancePoseController;
import dev.frozenmilk.dairy.core.util.controller.implementation.DoublePoseController;
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleVectorController;
import dev.frozenmilk.dairy.core.util.controller.implementation.UnitController;
import dev.frozenmilk.dairy.core.util.controller.implementation.DistanceVectorController;
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents;
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier;
import dev.frozenmilk.util.units.distance.Distance;
import dev.frozenmilk.util.units.distance.DistanceUnits;
import dev.frozenmilk.util.units.distance.Distances;

@SuppressWarnings("unused")
public class JavaOverview extends OpMode {
	double doubleControllerTarget;
	Distance unitControllerTarget;
	@Override
	public void init() {
		DcMotorEx motor = hardwareMap.get(DcMotorEx.class, "m");
		EnhancedDoubleSupplier doubleEncoder = new EnhancedDoubleSupplier(() -> (double) motor.getCurrentPosition());
		//
		// Controllers
		//
		// ComplexControllers are the Dairy alternative to run modes, allowing for powerful, extensible control loops
		
		doubleControllerTarget = 100.0; // we'll use this to control the target
		DoubleController doubleController = new DoubleController(
				// target
				// NaN can be returned for a component if you want to completely ignore it
				// but usually something else is better: 0.0, NEGATIVE_INFINITY, POSITIVE_INFINITY
				// in this case we're only ever going to use state for a calculation
				component -> {
					if (component == MotionComponents.STATE) {
						return doubleControllerTarget;
					}
					return 0.0;
				},
				// state
				// we'll use the motor's encoder for feedback
				doubleEncoder,
				// tolerance
				// when we check if we're finished, this is our default allowable error
				// NaN can be returned for a component if you want to completely ignore it
				// this cached wrapper will prevent regenerating the outputs, as they aren't dynamic
				new CachedMotionComponentSupplier<>(
						component -> {
							switch (component) {
								case STATE:
									return 10.0;
								case VELOCITY:
									return 1.0;
							}
							return Double.NaN; // we're going to ignore the rest of the tolerances
						}
				),
				// optional, callback
				motor::setPower, // when this controller updates, this callback will be run
				// then we build up the calculation:
				new DoubleComponent.P(MotionComponents.STATE, 0.1) // first P
						.plus(new DoubleComponent.I(MotionComponents.STATE, -0.00003, -0.1, 0.1)) // then I
						.plus(new DoubleComponent.D(MotionComponents.STATE, 0.0005)) // then D
		);
		// we can reset the internal state of the calculation components
		doubleController.getControllerCalculation().reset();
		
		// cool!, we just built a PID controller
		// this will automatically update in the background
		// we can check out how its going
		doubleController.finished(); // if within acceptable error of the target, determined using the toleranceEpsilon the controller current holds
		{
			// we can get it out
			MotionComponentSupplier<? extends Double> toleranceEpsilon = doubleController.getToleranceEpsilon();
			// or set it!
			doubleController.setToleranceEpsilon(component -> Double.NaN);
			doubleController.setToleranceEpsilon(toleranceEpsilon);
		}
		// or we can supply our own temporarily
		doubleController.finished(motionComponent -> Double.NaN);
		// double controllers are configured so that the appearance of NaN anywhere
		// in the finished check for a component will return true
		// so the above will return true
		
		// its easy to define your own calculation components,
		// take a look at the accompanying overview / docs for an example
		
		// there are also unit based controller systems in dairy
		
		EnhancedUnitSupplier<Distance> distanceEncoder = new EnhancedUnitSupplier<>(() -> new Distance(DistanceUnits.MILLIMETER, doubleEncoder.state()));
		unitControllerTarget = Distances.cm(10);
		new UnitController<>(
				component -> {
					if (component == MotionComponents.STATE) {
						return unitControllerTarget;
					}
					return Distances.mm(0);
				},
				distanceEncoder,
				// tolerance
				// when we check if we're finished, this is our default allowable error
				// NaN can be returned for a component if you want to completely ignore it
				// this cached wrapper will prevent regenerating the outputs, as they aren't dynamic
				new CachedMotionComponentSupplier<>(
						component -> {
							switch (component) {
								case STATE:
									return Distances.cm(10);
								case VELOCITY:
									return Distances.inch(1);
							}
							return Distances.mm(Double.NaN); // we're going to ignore the rest of the tolerances
						}
				),
				new UnitComponent.P<Distance>(MotionComponents.STATE, 0.5)
						.plus(
								BinaryBranch.<Distance>evalTrue(
										// there are also branching utilities in controllers
										ControllerComponents.targetComponent(target -> target.get(MotionComponents.STATE).greaterThan(Distances.ft(1))),
										// this be added if the target is larger than 1 foot
										new UnitComponent.D<>(MotionComponents.STATE, 2.0)
								)
								// we'll look more at branching utilities shortly
								// also, notice that we didn't add an outputConsumer? this controller is just going to do the calculations for it, it won't update anything
						)
		);
		
		// now we have a PID on position controller, that produces a target velocity output, and all we need to do to update both systems, is change the position target
		unitControllerTarget = new Distance(); // 0
		
		// you might want to stop a controller from running for a bit, e.g. if you need to switch to manual control mode
		doubleController.setEnabled(false);
		
		// for example, this stops double controller from updating in the background
		
		//
		// Branching
		//
		
		// binary branches get all the same arguments as ControllerCalculations, but return a boolean instead
		Distance twoIn = Distances.inch(2); // best not to reallocate this every loop
		BinaryBranch.<Distance>eval(
				// branching function
				(accumulation, state, target, error, deltaTime) -> {
					// returns true if the velocity error is greater than 2" / s
					return error.get(MotionComponents.VELOCITY).greaterThan(twoIn);
				},
				// true, lets use a larger kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.005),
				// false, lets use a smaller kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.0035)
		);
		
		// we could simplify above, using the errorComponent utilities
		
		// we'll use this for the following examples
		ControllerComponent<Distance, Boolean> simpleBinaryBranch =
				ControllerComponents.errorComponent(error -> {
					// returns true if the velocity error is greater than 2" / s
					return error.get(MotionComponents.VELOCITY).greaterThan(twoIn);
				});
		
		// simplifying above
		BinaryBranch.eval(
				// branching function
				simpleBinaryBranch,
				// true, lets use a larger kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.005),
				// false, lets use a smaller kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.0035)
		);
		
		BinaryBranch.evalTrue(
				// branching function
				simpleBinaryBranch,
				// true, lets use a larger kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.005)
				// false is a no-op
		);
		
		BinaryBranch.evalFalse(
				// branching function
				simpleBinaryBranch,
				// true is a no-op
				// false, lets use a smaller kP
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.0035)
		);
		
		// non-binary branches can be built using a map
		// see below for the enum used here
		Distance oneM = Distances.m(1);
		ControllerComponent<Distance, Branches> simpleBranch = ControllerComponents.errorComponent(error -> {
			Distance stateError = error.get(MotionComponents.STATE);
			if (stateError.greaterThan(twoIn)) return Branches.FIRST;
			else if (stateError.greaterThan(oneM)) return Branches.SECOND;
			return Branches.THIRD;
		});
		
		EnumMap<Branches, ControllerCalculation<Distance>> map = new EnumMap<Branches, ControllerCalculation<Distance>>(Branches.class){{
			put(Branches.FIRST, new UnitComponent.P<>(MotionComponents.VELOCITY, 0.0035));
			put(Branches.SECOND, new UnitComponent.P<>(MotionComponents.VELOCITY, 0.5));
			put(Branches.THIRD, new UnitComponent.P<>(MotionComponents.VELOCITY, 0.002));
		}};
		
		// map will run the calculation found in the map for the key determined by the branching component
		// if that key is null, then its a no-op
		Branch.map(
				simpleBranch,
				map
		);
		// mapOrDefault allows us to supply a default operation if simpleBranch
		// was to ever emit a Branches we don't have an item in the map for
		Branch.mapOrDefault(
				simpleBranch,
				map,
				new UnitComponent.P<>(MotionComponents.VELOCITY, 0.002)
		);
		
		// The joys of function composition allow you to do pretty much anything you want with this system,
		// and run on any hardware api platform.
		
		// In the future, Dairy should provide more default components to use, and types to use
		// along with tools for more complicated controller setup
		
		// supported in addition to Doubles and Units:
		DistanceVectorController distanceVectorController;
		DistancePoseController distancePoseController;
		DoubleVectorController doubleVectorController;
		DoublePoseController doublePoseController;
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
