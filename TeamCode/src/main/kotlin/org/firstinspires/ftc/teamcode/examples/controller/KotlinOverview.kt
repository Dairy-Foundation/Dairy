package org.firstinspires.ftc.teamcode.examples.controller

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.BinaryBranch
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.Branch
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.DoubleComponent
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.UnitComponent
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleController
import dev.frozenmilk.dairy.core.util.controller.implementation.PoseController
import dev.frozenmilk.dairy.core.util.controller.implementation.UnitController
import dev.frozenmilk.dairy.core.util.controller.implementation.VectorController
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit
import dev.frozenmilk.util.units.distance.DistanceUnits
import dev.frozenmilk.util.units.distance.feet
import dev.frozenmilk.util.units.distance.inches
import dev.frozenmilk.util.units.distance.mm

class KotlinOverview : OpMode() {
	private var targetDistance = 10.mm
	override fun init() {
		val motor: DcMotorEx = hardwareMap.get(DcMotorEx::class.java, "m")
		val doubleEncoder = EnhancedDoubleSupplier({ motor.currentPosition.toDouble() })
		//
		// Controllers
		//
		// ComplexControllers are the Dairy alternative to run modes, allowing for powerful, extensible control loops

		val doubleController = DoubleController(
			target = 0.0, // we can change our target again later
			inputSupplier = doubleEncoder, // we'll use the motor's encoder for feedback
			motionComponent = MotionComponents.POSITION, // we're going to work with position
			toleranceEpsilon = 0.001, // when we check finished, this is our standard allowable error
			outputConsumer = motor::setPower, // when this controller updates, this callback will be run
			// then we build up the calculation:
			controllerCalculation = DoubleComponent.P(0.1) + // first P
					DoubleComponent.I(-0.00003, -0.1, 0.1) + // then I
					DoubleComponent.D(0.0005) // then D
		)

		// cool!, we just built a PID controller
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
		val calculation =
				// its simple to write your own lambda / class implementation of the controller calculation
				// due to the type system, it is necessary for implementations to do the summation process themselves
				// so remember to return accumulation + the output you found
				ControllerCalculation { accumulation: Double, currentState: Double, target: Double, error: Double, deltaTime: Double ->
					accumulation + (error / 2) * currentState
				} + DoubleComponent.P(0.05) // and we can easily compose it with others!

		// there are also unit based controller systems

		// note that its also easy to pipe the output of one controller to another!
		// this controller just produces an output, for another one
		val distanceEncoder = EnhancedUnitSupplier({ Distance(DistanceUnits.MILLIMETER, doubleEncoder.position) })
		val positionController = UnitController(
			targetSupplier = { targetDistance }, 	// instead of a static target, we can use a dynamic one, now,
													// the target will update when we change the local variable
			inputSupplier = distanceEncoder,
			motionComponent = MotionComponents.POSITION,
			toleranceEpsilon = 0.2.inches,
			controllerCalculation = UnitComponent.P<DistanceUnit, Distance>(0.5) +
					BinaryBranch.Target<Distance> { it > 1.feet } // there are also branching utilities in controllers
						.forceEvalTrue(UnitComponent.D(2.0)) // this be added if the target is larger than 1 foot
					// we'll look more at branching utilities shortly
			// also, notice that we didn't add an outputConsumer? this controller is just going to do the calculations for it, it won't update anything
		)

		val veloController = UnitController(
			targetSupplier = positionController::output, // we're going to use the output of the position PID we just put together, to drive this velocity controller
			inputSupplier = distanceEncoder, // we're using the same distance encoder
			motionComponent = MotionComponents.VELOCITY, // this time we're working with velocity
			toleranceEpsilon = 0.2.inches,
			outputConsumer = motor::setPower,
			controllerCalculation = UnitComponent.P(0.5)
		)

		// now we have a PID on position controller, that produces a target velocity output, and all we need to do to update both systems, is change the position target
		targetDistance = Distance() // 0

		// you might want to stop a controller from running for a bit, e.g. if you need to switch to manual control mode
		doubleController.enabled = false

		// for example, this stops double controller from updating in the background

		//
		// Branching
		//

		// binary branches get all the same arguments as ControllerCalculations, but return a boolean instead
		val binaryBranch = BinaryBranch<Double> { accumulation, currentState, target, error, deltaTime ->
			deltaTime > 0.2
		}

		// BinaryBranch has helpful subclasses for working with a single component:
		val simpleBranch = BinaryBranch.Target<Double> { it > 0.0 } // we'll use this binary branch for the following examples.

		// once we have a binary branch, we can add Components to be conditionally added to the composed calculation
		simpleBranch.evalTrue(DoubleComponent.P(0.0)) // gets added if the binaryBranch evaluates to true
		simpleBranch.evalFalse(DoubleComponent.P(0.0)) // gets added if the binaryBranch evaluates to false
		simpleBranch.eval(
			evalTrue = DoubleComponent.P(0.0), // gets added if the binaryBranch evaluates to true
			evalFalse = DoubleComponent.P(0.0) // gets added if the binaryBranch evaluates to false
		)

		// each of the forceEval functions will run its component's evaluation function regardless of its used,
		// but will ignore the result,

		// this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
		// e. g. D terms where previous error and delta time may be differently timestamped
		simpleBranch.forceEvalTrue(DoubleComponent.P(0.0))
		simpleBranch.forceEvalFalse(DoubleComponent.P(0.0))
		simpleBranch.forceEval(
			DoubleComponent.P(0.0), // evalTrue
			DoubleComponent.P(0.0) // evalFalse
		)

		// non-binary branches use a map
		val branch = Branch<Double, Branches> { accumulation, currentState, target, error, deltaTime ->
			when {
				currentState > 0 -> Branches.FIRST
				currentState < 0 -> Branches.SECOND
				else -> Branches.THIRD
			}
		}

		// Branch also has the same helper subclasses
		// this is the same as above
		Branch.State<Double, Branches> {
			when {
				it > 0 -> Branches.FIRST
				it < 0 -> Branches.SECOND
				else -> Branches.THIRD
			}
		}

		// we need a map for branch to look up in
		// this map is missing Branches.THIRD, which means that branch will be a no-op or default
		val condMap = mapOf(
			Branches.FIRST to DoubleComponent.P(0.0),
			Branches.SECOND to DoubleComponent.P(0.0),
		)

		branch.map(condMap) // runs our condition, and then looks up the resulting component in the map
		branch.mapOrElse(condMap, DoubleComponent.P(0.0)) // runs the default if there would be a no-op

		branch.forceMap(condMap) // like the force variations of BinaryBranch, runs all components regardless of which is used
		branch.forceMapOrElse(condMap, DoubleComponent.P(0.0)) // runs the default if there would be a no-op, default is also force evaluated

		// The joys of function composition allow you to do pretty much anything you want with this system,
		// and run on any hardware api platform.

		// In the future, Dairy should provide more default components to use, and types to use
		// along with tools for more complicated controller setup

		// supported in addition to Doubles and Units:
		var vectorController: VectorController
		var poseController: PoseController
	}

	private enum class Branches {
		FIRST,
		SECOND,
		THIRD,
	}

	override fun loop() {
	}
}