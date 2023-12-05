package dev.frozenmilk.dairy.core

import android.icu.util.Output
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.core.cell.RefCell

/**
 * stores a set of dependency resolution rules for a feature
 *
 * a feature is enabled if all of its dependencies resolve
 */
open class DependencySet internal constructor(private val feature: Feature, dependencies: Set<Dependency<*, *>>) : Set<Dependency<*, *>> by dependencies {
	constructor(feature: Feature) : this(feature, setOf())

	/**
	 * non-mutating
	 *
	 * @see [includesAtLeastOneOf]
	 * @see [includesExactlyOneOf]
	 * @see [excludesFlags]
	 * @see [dependsOn]
	 * @see [mutuallyExclusiveWith]
	 */
	fun <T> withDependency(dependency: Dependency<T, *>): DependencySet {
		if (dependency.feature != this.feature) throw IllegalArgumentException("attempted to add a dependency that does not share the same feature as the others")
		return DependencySet(feature, this.plus(dependency))
	}

	/**
	 * non-mutating
	 *
	 * @see [IncludesAtLeastOneOf]
	 */
	fun includesAtLeastOneOf(vararg flags: Class<out Annotation>): FlagBoundDependencySet {
		return withDependency(IncludesAtLeastOneOf(feature, *flags)) as FlagBoundDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [IncludesExactlyOneOf]
	 */
	fun includesExactlyOneOf(vararg flags: Class<out Annotation>): FlagBoundDependencySet {
		return withDependency(IncludesExactlyOneOf(feature, *flags)) as FlagBoundDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [ExcludesFlags]
	 */
	fun excludesFlags(vararg flags: Class<out Annotation>): FlagBoundDependencySet {
		return withDependency(ExcludesFlags(feature, *flags)) as FlagBoundDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [DependsOn]
	 */
	fun dependsOn(vararg features: Feature): FeatureBoundDependencySet {
		return withDependency(DependsOn(feature, *features)) as FeatureBoundDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [DependsOn]
	 */
	fun mutuallyExclusiveWith(vararg features: Feature): FeatureBoundDependencySet {
		return withDependency(MutuallyExclusiveWith(feature, *features)) as FeatureBoundDependencySet
	}
}

class FlagBoundDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputCell: RefCell<Set<Class<out Annotation>>>) {
		(last() as FlagDependency).bindOutput(outputCell)
	}
}

class FeatureBoundDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputCell: RefCell<Set<Feature>>) {
		(last() as FeatureDependency).bindOutput(outputCell)
	}
}

class DependencyResolutionFailureException(private val feature: Feature, message: String) : RuntimeException("Failed to resolve dependencies for ${feature.javaClass.simpleName} as " + message) {
	override fun equals(other: Any?): Boolean {
		if (other !is DependencyResolutionFailureException) return false
		return this.feature == other.feature && this.message == other.message
	}

	override fun hashCode(): Int {
		return TODO()
	}
}

sealed interface Dependency<OUTPUT, ARGS> {
	/**
	 * the feature which this resolves to
	 */
	val feature: Feature

	/**
	 * returns true if this resolves against the found arguments
	 */
	fun resolves(args: ARGS): Boolean

	/**
	 * throws an error if the dependency doesn't resolve that contains some helpful diagnostic information
	 * @see[DependencyResolutionFailureException]
	 */
	fun resolvesOrError(args: ARGS): Boolean {
		if (!resolves(args)) throw DependencyResolutionFailureException(feature, dependencyResolutionFailureMessage)
		return true
	}

	val dependencyResolutionFailureMessage: String

	/**
	 * validates arguments, can be expensive to run
	 */
	fun validateContents()

	fun bindOutput(output: RefCell<OUTPUT>) {
		outputRef = output
	}

	var outputRef: RefCell<OUTPUT>?
	fun acceptResolutionOutput(output: OUTPUT) {
		outputRef?.accept(output)
	}
}

abstract class FlagDependency(override val feature: Feature, protected vararg val flags: Class<out Annotation>) : Dependency<Set<Class<out Annotation>>, Set<Class<out Annotation>>> {
	final override fun validateContents() {
		if (
				TeleOp::class.java in flags ||
				Autonomous::class.java in flags ||
				Disabled::class.java in flags
		)
			throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: annotations that are used as part of the base sdk are illegal flag dependency arguments")
	}

	override var outputRef: RefCell<Set<Class<out Annotation>>>? = null
}

abstract class FeatureDependency(override val feature: Feature, protected vararg val features: Feature) : Dependency<Set<Feature>, Set<Feature>> {
	final override fun validateContents() {
		if (feature in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
		if (features.any { dependency ->
					dependency.dependencies.filterIsInstance<FeatureDependency>()
							.all { it.features.contains(feature) }
				}) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it depends/is mutually exclusive with a feature that depends on it")
	}

	override var outputRef: RefCell<Set<Feature>>? = null
}

/**
 * resolves if the flags on the OpMode contain at least one of these Flags
 */
class IncludesAtLeastOneOf(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Set<Class<out Annotation>>): Boolean {
		return flags.any {
			it in args
		}
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("found flags did not include at least one of the following: ")
			flags.forEachIndexed { index, it ->
				append(it)
				if (index < flags.size) append(", ")
				else append(".")
			}
		}
	}
}

/**
 * resolves if the flags on the OpMode contain none of these Flags
 */
class ExcludesFlags(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Set<Class<out Annotation>>): Boolean {
		return flags.all {
			it !in args
		}
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("found flags included an excluded flag from the following: ")
			flags.forEachIndexed { index, it ->
				append(it)
				if (index < flags.size) append(", ")
				else append(".")
			}
		}
	}
}

/**
 * resolves if the flags on the OpMode contain exactly one of these Flags
 */
class IncludesExactlyOneOf(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Set<Class<out Annotation>>): Boolean {
		return flags.filter {
			it in args
		}.size != 1
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("found flags did not include exactly one of the following: ")
			flags.forEachIndexed { index, it ->
				append(it)
				if (index < flags.size) append(", ")
				else append(".")
			}
		}
	}
}

/**
 * ensures the arguments are attached and processed first before this, otherwise doesn't attach it
 */
class DependsOn(feature: Feature, vararg features: Feature) : FeatureDependency(feature, *features) {
	override fun resolves(args: Set<Feature>): Boolean {
		return features.all { it in args }
	}

	override val dependencyResolutionFailureMessage: String by lazy {
		buildString {
			append("the following required features were not all activated: ")
			features.forEachIndexed { index, it ->
				append(it)
				if (index < features.size) append(", ")
				else append(".")
			}
		}
	}
}

class MutuallyExclusiveWith(feature: Feature, vararg features: Feature) : FeatureDependency(feature, *features) {
	override fun resolves(args: Set<Feature>): Boolean {
		return features.all { it !in args }
	}

	override val dependencyResolutionFailureMessage: String by lazy {
		buildString {
			append("any of the following excluded features were activated: ")
			features.forEachIndexed { index, it ->
				append(it)
				if (index < features.size) append(", ")
				else append(".")
			}
		}
	}
}

