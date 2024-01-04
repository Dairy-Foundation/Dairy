package dev.frozenmilk.dairy.calcified.hardware.controller

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.calcified.hardware.motor.MotorControllerGroup
import dev.frozenmilk.dairy.calcified.hardware.motor.SimpleMotor
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.angle.Angle
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
        set(value) {
            if (value && !field) {
                FeatureRegistrar.registerFeature(this)
            }
            else if (!value && field) {
                FeatureRegistrar.deregisterFeature(this)
            }
            field = value
        }
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
            LateInitCell(error = "Error supplier cannot be inferred, try attaching one first"),
            LateInitCell(error = "Position supplier cannot be inferred, try attaching one first"))


    /**
     * non-mutating
     *
     * attaches the motors to the controller
     */
    fun addMotors(vararg motors: SimpleMotor): LambdaController<IN> {
        return LambdaController(this.target, MotorControllerGroup(*motors), this.calculators, lastErrorSupplier, lastPositionSupplier)
    }

    /**
     * uses this error supplier for future error based controllers, until a new one is supplied
     */
    fun withErrorSupplier(errorSupplier: ErrorSupplier<IN, Double>): LambdaController<IN> {
        lastErrorSupplier.accept(errorSupplier)
        return this
    }

    /**
     * uses this position supplier for future position based controllers, until a new one is supplied
     */
    fun withPositionSupplier(positionSupplier: Supplier<IN>): LambdaController<IN> {
        lastPositionSupplier.accept(positionSupplier)
        return this
    }

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

}
fun LambdaController<Int>.appendProfiledController(positionSupplier: Supplier<Int>, constraints: ProfileConstraints, component: ProfileStateComponent)
        = appendController(object : CalculatorComponent<Int> {
    private var profile = AsymmetricMotionProfile(positionSupplier.get(), target, constraints)
    private var startTime = System.nanoTime()
    override fun calculate(target: Int, deltaTime: Double): Double {
        if(target != profile.final) {
            profile = AsymmetricMotionProfile(positionSupplier.get(), target, constraints)
            startTime = System.nanoTime()
        }
        return profile.calculate((System.nanoTime() - startTime) / 1E9, component)
    }
})

fun LambdaController<Int>.appendProfiledController(constraints: ProfileConstraints, component: ProfileStateComponent)
        = appendProfiledController(lastPositionSupplier.get(), constraints, component)
fun <A : Angle> LambdaController<A>.appendArmFFController(armFFController: ArmFFController<A>) = appendController(armFFController)
fun <A : Angle> LambdaController<A>.appendArmFFController(kF: Double) = appendArmFFController(ArmFFController(lastPositionSupplier.get(), kF))
