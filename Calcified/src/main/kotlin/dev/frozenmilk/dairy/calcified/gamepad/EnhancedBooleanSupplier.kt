package dev.frozenmilk.dairy.calcified.gamepad

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import java.util.function.Supplier

class EnhancedBooleanSupplier(private val parents: Collection<Feature>, private val booleanSupplier: Supplier<Boolean>, private val risingDebounce: Long, private val fallingDebounce: Long) : Supplier<Boolean>, Feature {
	private constructor(parents: Collection<Feature>, booleanSupplier: Supplier<Boolean>, other: Supplier<Boolean>?) : this(
			parents.also {
				if (booleanSupplier is Feature) it + booleanSupplier
			}
			.also {
				if (other is Feature) it + booleanSupplier
			},
			booleanSupplier,
			0, 0
	)
	// todo review and potentially change to be lazy
	constructor(booleanSupplier: Supplier<Boolean>) : this(emptyList(), booleanSupplier, null)
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
	fun debounce(debounce: Double) = EnhancedBooleanSupplier(parents + this, this.booleanSupplier, (debounce * 1E9).toLong(), (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param rising is applied to the rising edge
	 * @param falling is applied to the falling edge
	 */
	fun debounce(rising: Double, falling: Double) = EnhancedBooleanSupplier(parents + this, this.booleanSupplier, (rising * 1E9).toLong(), (falling * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the rising edge
	 */
	fun debounceRisingEdge(debounce: Double) = EnhancedBooleanSupplier(parents + this, this.booleanSupplier, (debounce * 1E9).toLong(), this.fallingDebounce)

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the falling edge
	 */
	fun debounceFallingEdge(debounce: Double) = EnhancedBooleanSupplier(parents + this, this.booleanSupplier, this.risingDebounce, (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun and(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier (parents + this, { this.get() and booleanSupplier.get() }, booleanSupplier)

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun or(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier (parents + this, { this.get() or booleanSupplier.get() }, booleanSupplier)

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun xor(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier (parents + this, { this.get() xor booleanSupplier.get() }, booleanSupplier)

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that has the inverse of this
	 */
	operator fun not() = EnhancedBooleanSupplier (parents + this, { !this.get() }, null)

	//
	// Impl Feature:
	//
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this).also {
		if (parents.isNotEmpty()) {
			it.dependsDirectlyOn(*parents.toTypedArray())
		}
		else {
			it.yields()
		}
	}

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
		update()
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
		update()
	}
}