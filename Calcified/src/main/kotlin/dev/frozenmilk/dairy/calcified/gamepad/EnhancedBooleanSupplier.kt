package dev.frozenmilk.dairy.calcified.gamepad

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import java.util.function.Supplier

open class EnhancedBooleanSupplier(private val booleanSupplier: Supplier<Boolean>, private val risingDebounce: Long, private val fallingDebounce: Long) : Supplier<Boolean>, Feature {
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
		if(!current && booleanSupplier.get() && time - timeMarker > risingDebounce){
			previous = false
			current = true
			timeMarker = time
			toggleTrue = !toggleTrue
		}
		else if (current && !booleanSupplier.get() && time - timeMarker > fallingDebounce) {
			previous = true
			current = false
			timeMarker = time
			toggleFalse = !toggleFalse
		}
	}

	override fun get(): Boolean { return current }
	val whenTrue: Boolean get() { return current and !previous }
	val whenFalse: Boolean get() { return !current and previous }

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to both the rising and falling edges
	 */
	fun debounce(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param rising is applied to the rising edge
	 * @param falling is applied to the falling edge
	 */
	fun debounce(rising: Double, falling: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (rising * 1E9).toLong(), (falling * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the rising edge
	 */
	fun debounceRisingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), this.fallingDebounce)

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the falling edge
	 */
	fun debounceFallingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, this.risingDebounce, (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun and(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() and booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun or(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() or booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun xor(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() xor booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that has the inverse of this
	 */
	operator fun not() = EnhancedBooleanSupplier { !this.get() }

	//
	// Impl Feature:
	//
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