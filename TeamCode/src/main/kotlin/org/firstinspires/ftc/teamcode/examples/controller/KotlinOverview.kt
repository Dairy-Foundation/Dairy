package org.firstinspires.ftc.teamcode.examples.controller

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import dev.frozenmilk.dairy.core.util.controller.calculation.errorComponent
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.eval
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.evalFalse
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.evalTrue
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.map
import dev.frozenmilk.dairy.core.util.controller.calculation.logical.mapOrDefault
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.DoubleComponent
import dev.frozenmilk.dairy.core.util.controller.calculation.pid.UnitComponent
import dev.frozenmilk.dairy.core.util.controller.calculation.targetComponent
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleController
import dev.frozenmilk.dairy.core.util.controller.implementation.DistancePoseController
import dev.frozenmilk.dairy.core.util.controller.implementation.UnitController
import dev.frozenmilk.dairy.core.util.controller.implementation.DistanceVectorController
import dev.frozenmilk.dairy.core.util.controller.implementation.DoublePoseController
import dev.frozenmilk.dairy.core.util.controller.implementation.DoubleVectorController
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnits
import dev.frozenmilk.util.units.distance.cm
import dev.frozenmilk.util.units.distance.ft
import dev.frozenmilk.util.units.distance.inch
import dev.frozenmilk.util.units.distance.m
import dev.frozenmilk.util.units.distance.mm
import java.util.EnumMap
import java.util.function.Function

@Suppress("unused")
class KotlinOverview : OpMode() {
	private var doubleControllerTarget = 0.0
	private var unitControllerTarget = Distance()
	override fun init() {
		val motor = hardwareMap.get(DcMotorEx::class.java, "m")
		val doubleEncoder = EnhancedDoubleSupplier {
			motor.currentPosition.toDouble()
		}

		//
		// Controllers
		//
		// ComplexControllers are the Dairy alternative to run modes, allowing for powerful, extensible control loops
		doubleControllerTarget = 100.0 // we'll use this to control the target
		val doubleController = DoubleController(
			// target
			// NaN can be returned for a component if you want to completely ignore it
			// but usually something else is better: 0.0, NEGATIVE_INFINITY, POSITIVE_INFINITY
			// in this case we're only ever going to use state for a calculation
			targetSupplier = MotionComponentSupplier {
				if (it == MotionComponents.STATE) {
					return@MotionComponentSupplier doubleControllerTarget
				}
				0.0
			},
			// state
			// we'll use the motor's encoder for feedback
			stateSupplier = doubleEncoder,
			// tolerance
			// when we check if we're finished, this is our default allowable error
			// NaN can be returned for a component if you want to completely ignore it
			// this cached wrapper will prevent regenerating the outputs, as they aren't dynamic
			toleranceEpsilon = CachedMotionComponentSupplier(
				MotionComponentSupplier {
					return@MotionComponentSupplier when (it) {
						MotionComponents.STATE -> 10.0
						MotionComponents.VELOCITY -> 1.0
						else -> Double.NaN
					}
				}
			),
			// optional, callback
			outputConsumer = motor::setPower, // when this controller updates, this callback will be run
			// then we build up the calculation:
			controllerCalculation = DoubleComponent.P(MotionComponents.STATE, 0.1) // first P
				.plus(DoubleComponent.I(MotionComponents.STATE, -0.00003, -0.1, 0.1)) // then I
				.plus(DoubleComponent.D(MotionComponents.STATE, 0.0005)) // then D
		)
		// we can reset the internal state of the calculation components
		doubleController.controllerCalculation.reset()


		// cool!, we just built a PID controller
		// this will automatically update in the background
		// we can check out how its going
		doubleController.finished() // if within acceptable error of the target, determined using the toleranceEpsilon the controller current holds
		run {
			// we can get it out
			val toleranceEpsilon = doubleController.toleranceEpsilon
			// or set it!
			doubleController.toleranceEpsilon = MotionComponentSupplier { Double.NaN }
			doubleController.toleranceEpsilon = toleranceEpsilon
		}
		// or we can supply our own temporarily
		doubleController.finished { Double.NaN }

		// double controllers are configured so that the appearance of NaN anywhere
		// in the finished check for a component will return true
		// so the above will return true

		// its easy to define your own calculation components,
		// take a look at the accompanying overview / docs for an example

		// there are also unit based controller systems in dairy
		val distanceEncoder = EnhancedUnitSupplier {
			Distance(
				DistanceUnits.MILLIMETER,
				doubleEncoder.state
			)
		}
		unitControllerTarget = 10.cm
		UnitController(
			targetSupplier = MotionComponentSupplier {
				return@MotionComponentSupplier if (it == MotionComponents.STATE)
					unitControllerTarget
				else 0.mm
			},
			stateSupplier = distanceEncoder,
			// tolerance
			// when we check if we're finished, this is our default allowable error
			// NaN can be returned for a component if you want to completely ignore it
			// this cached wrapper will prevent regenerating the outputs, as they aren't dynamic
			toleranceEpsilon = CachedMotionComponentSupplier(
				MotionComponentSupplier {
					return@MotionComponentSupplier when (it) {
						MotionComponents.STATE -> 10.cm
						MotionComponents.VELOCITY -> 1.inch
						else -> Double.NaN.mm // we're going to ignore the rest of the tolerances
					}
				}
			),
			controllerCalculation = UnitComponent.P<Distance>(MotionComponents.STATE, 0.5) +
					// there are also branching utilities in controllers
					Function<MotionComponentSupplier<out Distance>, Boolean> {
						it[MotionComponents.STATE] > 1.ft
					}.targetComponent() // this be added if the target is larger than 1 foot
						.evalTrue(
							UnitComponent.D(MotionComponents.STATE, 2.0)
						) // we'll look more at branching utilities shortly
		)
		// also, notice that we didn't add an outputConsumer?
		// this controller is just going to do the calculations for it, it won't update anything


		// now we have a PID on position controller, that produces a target velocity output, and all we need to do to update both systems, is change the position target
		unitControllerTarget = Distance() // 0


		// you might want to stop a controller from running for a bit, e.g. if you need to switch to manual control mode
		doubleController.enabled = false


		// for example, this stops double controller from updating in the background

		//
		// Branching
		//

		// binary branches get all the same arguments as ControllerCalculations, but return a boolean instead
		val twoIn = 2.inch // best not to reallocate this every loop
		// branching function
		ControllerComponent { accumulation: Distance?, state: MotionComponentSupplier<out Distance>?, target: MotionComponentSupplier<out Distance>?, error: MotionComponentSupplier<out Distance>, deltaTime: Double ->
			error[MotionComponents.VELOCITY] > twoIn
		} // true, lets use a larger kP
			.eval(
				UnitComponent.P(MotionComponents.VELOCITY, 0.005),  // false, lets use a smaller kP
				UnitComponent.P(MotionComponents.VELOCITY, 0.0035)
			)

		// we could simplify above, using the errorComponent utilities

		// we'll use this for the following examples
		val simpleBinaryBranch =
			Function<MotionComponentSupplier<out Distance>, Boolean> {
				it[MotionComponents.VELOCITY] > twoIn
			}.errorComponent()


		// simplifying above
		// branching function
		simpleBinaryBranch // true, lets use a larger kP
			.eval(
				UnitComponent.P(MotionComponents.VELOCITY, 0.005),  // false, lets use a smaller kP
				UnitComponent.P(MotionComponents.VELOCITY, 0.0035)
			)

		// branching function
		simpleBinaryBranch // true, lets use a larger kP
			.evalTrue(
				UnitComponent.P(MotionComponents.VELOCITY, 0.005) // false is a no-op
			)

		// branching function
		simpleBinaryBranch // true is a no-op
			// false, lets use a smaller kP
			.evalFalse(
				UnitComponent.P(MotionComponents.VELOCITY, 0.0035)
			)


		// non-binary branches can be built using a map
		// see below for the enum used here
		val oneM = 1.m
		val simpleBranch =
			Function<MotionComponentSupplier<out Distance>, Branches> { error: MotionComponentSupplier<out Distance> ->
				val stateError = error[MotionComponents.STATE]
				return@Function if (stateError > twoIn) Branches.FIRST
				else if (stateError > oneM) Branches.SECOND
				else Branches.THIRD
			}.errorComponent()

		val map: EnumMap<Branches, ControllerCalculation<Distance>> = EnumMap<Branches, ControllerCalculation<Distance>>(Branches::class.java)
			.apply {
				this[Branches.FIRST] = UnitComponent.P(MotionComponents.VELOCITY, 0.0035)
				this[Branches.SECOND] = UnitComponent.P(MotionComponents.VELOCITY, 0.5)
				this[Branches.THIRD] = UnitComponent.P(MotionComponents.VELOCITY, 0.002)
			}


		// map will run the calculation found in the map for the key determined by the branching component
		// if that key is null, then its a no-op
		simpleBranch
			.map(
				map
			)
		// mapOrDefault allows us to supply a default operation if simpleBranch
		// was to ever emit a Branches we don't have an item in the map for
		simpleBranch
			.mapOrDefault(
				map,
				UnitComponent.P(MotionComponents.VELOCITY, 0.002)
			)


		// The joys of function composition allow you to do pretty much anything you want with this system,
		// and run on any hardware api platform.

		// In the future, Dairy should provide more default components to use, and types to use
		// along with tools for more complicated controller setup

		// supported in addition to Doubles and Units:
		val distanceVectorController: DistanceVectorController
		val distancePoseController: DistancePoseController
		val doubleVectorController: DoubleVectorController
		val doublePoseController: DoublePoseController
	}

	private enum class Branches {
		FIRST,
		SECOND,
		THIRD
	}

	override fun loop() {
	}
}