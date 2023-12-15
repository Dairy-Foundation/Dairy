package dev.frozenmilk.dairy.calcified.gamepad

import com.qualcomm.robotcore.hardware.Gamepad
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import java.util.function.DoubleSupplier
import java.util.function.Supplier

class CalcifiedGamepad(private val gamepad: Gamepad) {
	/**
	 * left analog stick horizontal axis
	 */
	var leftStickX = EnhancedDoubleSupplier { gamepad.left_stick_x.toDouble() }

	/**
	 * left analog stick vertical axis
	 */
	var leftStickY = EnhancedDoubleSupplier { gamepad.left_stick_y.toDouble() }

	/**
	 * right analog stick horizontal axis
	 */
	var rightStickX = EnhancedDoubleSupplier { gamepad.right_stick_x.toDouble() }

	/**
	 * right analog stick vertical axis
	 */
	var rightStickY = EnhancedDoubleSupplier { gamepad.right_stick_y.toDouble() }

	/**
	 * dpad up
	 */
	var dpadUp = EnhancedBooleanSupplier { gamepad.dpad_up }

	/**
	 * dpad down
	 */
	var dpadDown = EnhancedBooleanSupplier { gamepad.dpad_down }

	/**
	 * dpad left
	 */
	var dpadLeft = EnhancedBooleanSupplier { gamepad.dpad_left }

	/**
	 * dpad right
	 */
	var dpadRight = EnhancedBooleanSupplier { gamepad.dpad_right }

	/**
	 * button a
	 */
	var a = EnhancedBooleanSupplier { gamepad.a }

	/**
	 * button b
	 */
	var b = EnhancedBooleanSupplier { gamepad.b }

	/**
	 * button x
	 */
	var x = EnhancedBooleanSupplier { gamepad.x }

	/**
	 * button y
	 */
	var y = EnhancedBooleanSupplier { gamepad.y }

	/**
	 * button guide - often the large button in the middle of the controller. The OS may
	 * capture this button before it is sent to the app; in which case you'll never
	 * receive it.
	 */
	var guide = EnhancedBooleanSupplier { gamepad.guide }

	/**
	 * button start
	 */
	var start = EnhancedBooleanSupplier { gamepad.start }

	/**
	 * button back
	 */
	var back = EnhancedBooleanSupplier { gamepad.back }

	/**
	 * button left bumper
	 */
	var leftBumper = EnhancedBooleanSupplier { gamepad.left_bumper }

	/**
	 * button right bumper
	 */
	var rightBumper = EnhancedBooleanSupplier { gamepad.right_bumper }

	/**
	 * left stick button
	 */
	var leftStickButton = EnhancedBooleanSupplier { gamepad.left_stick_button }

	/**
	 * right stick button
	 */
	var rightStickButton = EnhancedBooleanSupplier { gamepad.right_stick_button }

	/**
	 * left trigger
	 */
	var leftTrigger = EnhancedDoubleSupplier { gamepad.left_trigger.toDouble() }

	/**
	 * right trigger
	 */
	var rightTrigger = EnhancedDoubleSupplier { gamepad.right_trigger.toDouble() }

	/**
	 * PS4 Support - Circle
	 */
	var circle
		get() = b
		set(value) {
			b = value
		}

	/**
	 * PS4 Support - cross
	 */
	var cross
		get() = a
		set(value) {
			a = value
		}

	/**
	 * PS4 Support - triangle
	 */
	var triangle
		get() = y
		set(value) {
			y = value
		}

	/**
	 * PS4 Support - square
	 */
	var square
		get() = x
		set(value) {
			x = value
		}

	/**
	 * PS4 Support - share
	 */
	var share
		get() = back
		set(value) {
			back = value
		}

	/**
	 * PS4 Support - options
	 */
	var options
		get() = start
		set(value) {
			start = value
		}

	/**
	 * PS4 Support - touchpad
	 */
	var touchpad = EnhancedBooleanSupplier { gamepad.touchpad }

	var touchpadFinger1 = EnhancedBooleanSupplier { gamepad.touchpad_finger_1 }

	var touchpadFinger2 = EnhancedBooleanSupplier { gamepad.touchpad_finger_2 }

	var touchpadFinger1X = EnhancedDoubleSupplier { gamepad.touchpad_finger_1_x.toDouble() }

	var touchpadFinger1Y = EnhancedDoubleSupplier { gamepad.touchpad_finger_1_y.toDouble() }

	var touchpadFinger2X = EnhancedDoubleSupplier { gamepad.touchpad_finger_2_x.toDouble() }

	var touchpadFinger2Y = EnhancedDoubleSupplier { gamepad.touchpad_finger_2_y.toDouble() }

	/**
	 * PS4 Support - PS Button
	 */
	var ps
		get() = guide
		set(value) {
			guide = value
		}
}

class EnhancedBooleanSupplier(private val booleanSupplier: Supplier<Boolean>, private val leadingDebounce: Long, private val trailingDebounce: Long) : Supplier<Boolean>, Feature {
	constructor(booleanSupplier: Supplier<Boolean>) : this(booleanSupplier, 0, 0)
	private var previous = booleanSupplier.get()
	private var current = booleanSupplier.get()
	var toggleTrue = booleanSupplier.get()
		private set
	var toggleFalse = booleanSupplier.get()
		private set
	private var timeMarker = 0L
	fun update() {
		val time = System.nanoTime()
		if(!current && booleanSupplier.get() && time - timeMarker > leadingDebounce){
			previous = false
			current = true
			timeMarker = time
			toggleTrue = !toggleTrue
		}
		else if (current && !booleanSupplier.get() && time - timeMarker > trailingDebounce) {
			previous = true
			current = false
			timeMarker = time
			toggleFalse = !toggleFalse
		}
	}

	override fun get(): Boolean = current
	val whenTrue: Boolean get() = current and !previous
	val whenFalse: Boolean get() = !current and previous

	/**
	 * non-mutating
	 */
	fun debounce(debounce: Long) : EnhancedBooleanSupplier = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 */
	fun debounce(leading: Long, trailing: Long) : EnhancedBooleanSupplier = EnhancedBooleanSupplier(this.booleanSupplier, leading, trailing)

	/**
	 * non-mutating
	 */
	fun debounceLeadingEdge(debounce: Long) : EnhancedBooleanSupplier = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), this.trailingDebounce)

	/**
	 * non-mutating
	 */
	fun debounceTrailingEdge(debounce: Long) : EnhancedBooleanSupplier = EnhancedBooleanSupplier(this.booleanSupplier, this.leadingDebounce, (debounce * 1E9).toLong())

	override val dependencies: Set<Dependency<*, *>> = DependencySet(this).yields()

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {
		update()
	}

	override fun postUserInitHook(opMode: OpModeWrapper) {}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStartHook(opMode: OpModeWrapper) {
		update()
	}

	override fun postUserStartHook(opMode: OpModeWrapper) {}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {}

	override fun preUserStopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {}
}

class EnhancedDoubleSupplier(private val doubleSupplier: Supplier<Double>, private val modify: (Double) -> Double = { x -> x }, private val lowerDeadzone: Double = 0.0, private val upperDeadzone: Double = 0.0) : Supplier<Double> {
	constructor(doubleSupplier: Supplier<Double>) : this(doubleSupplier, { x -> x })

	override fun get(): Double {
		val result = modify(doubleSupplier.get())
		if (result < 0.0 && result >= lowerDeadzone) return 0.0
		if (result > 0.0 && result <= upperDeadzone) return 0.0
		return result
	}

	/**
	 * non-mutating
	 */
	fun invert(): EnhancedDoubleSupplier = EnhancedDoubleSupplier({ -this.doubleSupplier.get() }, this.modify, this.lowerDeadzone, this.upperDeadzone)

	/**
	 * non-mutating
	 */
	fun applyDeadzone(deadzone: Double): EnhancedDoubleSupplier = EnhancedDoubleSupplier(this.doubleSupplier, this.modify, deadzone, deadzone)
	/**
	 * non-mutating
	 */
	fun applyDeadzone(lowerDeadzone: Double, upperDeadzone: Double): EnhancedDoubleSupplier = EnhancedDoubleSupplier(this.doubleSupplier, this.modify, lowerDeadzone, upperDeadzone)
	/**
	 * non-mutating
	 */
	fun applyLowerDeadzone(lowerDeadzone: Double): EnhancedDoubleSupplier = EnhancedDoubleSupplier(this.doubleSupplier, this.modify, lowerDeadzone, this.upperDeadzone)
	/**
	 * non-mutating
	 */
	fun applyUpperDeadzone(upperDeadzone: Double): EnhancedDoubleSupplier = EnhancedDoubleSupplier(this.doubleSupplier, this.modify, this.lowerDeadzone, upperDeadzone)
}

fun DoubleSupplier.conditionalBind(): Conditional = Conditional(this)