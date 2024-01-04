package dev.frozenmilk.dairy.core

import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * A [LazyCell] that is initialised on the init of an OpMode
 */
class OpModeLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.yields()

	override fun get(): T {
		if(!initialised() && !FeatureRegistrar.opModeActive) throw IllegalStateException("Attempted to evaluate contents of OpModeLazyCell while no opmode active")
		return super.get()
	}

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: OpModeWrapper) { get() }
}

@JvmName("CellUtils")
fun <T> Supplier<T>.intoOpModeLazyCell() = OpModeLazyCell(this)