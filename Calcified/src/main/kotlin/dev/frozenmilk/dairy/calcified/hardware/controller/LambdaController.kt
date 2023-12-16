package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.profile.AsymmetricMotionProfile
import dev.frozenmilk.util.profile.ProfileConstraints
import dev.frozenmilk.util.profile.ProfileStateComponent
import java.util.function.Supplier

class LambdaController<IN> internal constructor(override var target: IN,
                                                override val motors: SimpleMotor,
                                                override val calculators: List<CalculatorComponent<IN>>,
                                                val lastErrorSupplier: LateInitCell<ErrorSupplier<IN, Double>>,
                                                val lastPositionSupplier: LateInitCell<Supplier<IN>>) : ComplexController<IN> {
    override var autoUpdate: Boolean = false
    override var currentTime: Long = System.nanoTime()
    override var previousTime: Long = currentTime
    override val dependencies: Set<Dependency<*, *>> = DependencySet(this).yieldsTo(Calcified::class.java)

    /**
     * constructs a new Controller that always outputs 0
     */
    constructor(target: IN) : this(
            target,
            MotorControllerGroup(),
            emptyList(),
            LateInitCell("Error supplier cannot be inferred, try attaching one first"),
            LateInitCell("Position supplier cannot be inferred, try attaching one first"))

    /**
     * should be the last instruction in the building of your controller, putting it in earlier may cause issues
     */
    fun enableAutoPolling() {
        FeatureRegistrar.registerFeature(this)
        autoUpdate = true
    }

    fun addMotors(vararg motors: SimpleMotor): LambdaController<IN> {
        return LambdaController(this.target, MotorControllerGroup(*motors), this.calculators, lastErrorSupplier, lastPositionSupplier)
    }

//    fun <OUT> appendErrorBasedController(errorSupplier: ErrorSupplier<IN, OUT>, calculationComponent: (OUT) -> Double) : LambdaController<IN> {
//        return LambdaController(this.target, this.motors, this.calculators.plus { calculationComponent(errorSupplier.getError(target)) })
//    }
//    fun <OUT> appendPositionBasedController(positionSupplier: Supplier<OUT>, calculationComponent: (OUT) -> Double) : LambdaController<IN> {
//        return LambdaController(this.target, this.motors, this.errorCalculators, this.positionalCalculators.plus { calculationComponent(positionSupplier.get()) })
//    }

    fun appendController(calculatorComponent: CalculatorComponent<IN>): LambdaController<IN> {
        when (calculatorComponent) {
            is ErrorBasedCalculator -> {
                lastErrorSupplier.accept(calculatorComponent.errorSupplier)
            }
            is PositionBasedCalculator -> {
                lastPositionSupplier.accept(calculatorComponent.positionSupplier)
            }
        }
        return LambdaController(target, motors, calculators.plus(calculatorComponent), lastErrorSupplier, lastPositionSupplier)
    }
    fun appendPController(pController: PController<IN>) = appendController(pController)
    fun appendPController(kP: Double) = appendPController(PController(lastErrorSupplier.get(), kP))
    fun appendIController(iController: IController<IN>) = appendController(iController)
    fun appendIController(kI: Double, lowerLimit: Double, upperLimit: Double) = appendIController(IController(lastErrorSupplier.get(), kI, lowerLimit, upperLimit))
    fun appendDController(dController: DController<IN>) = appendController(dController)
    fun appendDController(kD: Double) = appendDController(DController(lastErrorSupplier.get(), kD))

//    fun appendPController(errorSupplier: ErrorSupplier<IN, Double>, kP: Double) = appendErrorBasedController(errorSupplier) { input: Double -> input * kP }
//
//    fun appendDController(errorSupplier: ErrorSupplier<IN, Double>, kP: Double) = appendErrorBasedController(errorSupplier) { input: Double -> input * kP }
//
//    fun appendFFController(positionSupplier: Supplier<Double>, kFF: Double) = appendPositionBasedController(positionSupplier) { input: Double -> input * kFF }
//
//    fun appendAngleFFController(positionSupplier: Supplier<Angle>, kFF: Double) = appendPositionBasedController(positionSupplier) { input: Angle -> kFF * cos(input.intoRadians().theta) }

}
fun LambdaController<Double>.profiledController(target: Double, positionSupplier: Supplier<Double>, constraints: ProfileConstraints, component: ProfileStateComponent)
        = appendController(object : CalculatorComponent<Double> {
    private var profile = AsymmetricMotionProfile(positionSupplier.get(), target, constraints)
    private var startTime = System.nanoTime()
    override fun calculate(target: Double, deltaTime: Double): Double {
        if(target != profile.final) {
            profile = AsymmetricMotionProfile(positionSupplier.get(), target, constraints)
            startTime = System.nanoTime()
        }
        return profile.calculate((System.nanoTime() - startTime) / 1E9, component)
    }
})

fun LambdaController<Double>.profiledController(target: Double, constraints: ProfileConstraints, component: ProfileStateComponent)
        = profiledController(target, lastPositionSupplier.get(), constraints, component)
