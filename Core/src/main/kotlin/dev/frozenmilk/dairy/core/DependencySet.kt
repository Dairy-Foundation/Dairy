package dev.frozenmilk.dairy.core

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.util.cell.SingleCell
import java.util.Objects
import java.util.function.Consumer

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
	 * @see [dependsOnOneOf]
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
	 * @see [DependsOnOneOf]
	 */
	fun dependsOnOneOf(vararg features: Class<out Feature>): SingleFeatureDependencySet {
		return withDependency(DependsOnOneOf(feature, *features)) as SingleFeatureDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [DependsOnOneOf]
	 */
	fun mutuallyExclusiveWith(vararg features: Class<out Feature>): FeatureBoundDependencySet {
		return withDependency(MutuallyExclusiveWith(feature, *features)) as FeatureBoundDependencySet
	}

	/**
	 * non-mutating
	 *
	 * @see [Yields]
	 */
	fun yields(): DependencySet {
		return withDependency(Yields(feature))
	}

	/**
	 * non-mutating
	 *
	 * @see [YieldsTo]
	 */
	fun yieldsTo(vararg features: Class<out Feature>): YieldsToFeatureBoundDependencySet {
		return withDependency(YieldsTo(feature, *features)) as YieldsToFeatureBoundDependencySet
	}
}

class FlagBoundDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Annotation>>): DependencySet {
		(last() as FlagDependency).bindOutput(outputConsumer)
		return this
	}
}

class YieldsToFeatureBoundDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Feature>>) : DependencySet {
		(last() as YieldsTo).bindOutput(outputConsumer)
		return this
	}
}
class FeatureBoundDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Feature>>): DependencySet{
		(last() as FeatureDependency).bindOutput(outputConsumer)
		return this
	}
}

class SingleFeatureDependencySet(feature: Feature, dependencies: Set<Dependency<*, *>>) : DependencySet(feature, dependencies) {
	fun bindOutputTo(outputConsumer: Consumer<Feature?>): DependencySet{
		(last() as DependsOnOneOf).bindOutput(outputConsumer)
		return this
	}
}

class FeatureDependencyResolutionFailureException(private val feature: Feature, message: String, reasons: Collection<String>) : RuntimeException(buildString {
	append("Failed to resolve dependencies for ${feature.javaClass.simpleName} as ")
	append(message)
	append(": ")
	reasons.forEachIndexed {
		index, it ->
		append(it)
		if (index < reasons.size) append(", ")
		else append(".")
	}
}) {
	override fun equals(other: Any?): Boolean {
		if (other !is FeatureDependencyResolutionFailureException) return false
		return this.feature == other.feature && this.message == other.message
	}

	override fun hashCode(): Int {
		return Objects.hash(feature, message)
	}
}

operator fun Exception.plus(other: Exception): Exception {
	return Exception(if(message?.isBlank() == true) this.message + "\n" + other.message else other.message)
}
class DependencyResolutionFailureException(message: String) : Exception(message)

sealed interface Dependency<OUTPUT, ARGS> {
	/**
	 * the feature which this resolves to
	 */
	val feature: Feature

	/**
	 * returns <true, resolving arguments> if this resolves against the found arguments
	 * returns <false, empty()> if this fails to resolve against the found arguments
	 */
	fun resolves(args: ARGS): Pair<Boolean, OUTPUT>

	/**
	 * throws an error if the dependency doesn't resolve that contains some helpful diagnostic information
	 * @see[FeatureDependencyResolutionFailureException]
	 */
	fun resolvesOrError(args: ARGS): Pair<Boolean, OUTPUT> {
		val resolution = resolves(args)
		if (!resolution.first) throw FeatureDependencyResolutionFailureException(feature, dependencyResolutionFailureMessage, failures)
		return resolution
	}

	val failures: Collection<String>
	val dependencyResolutionFailureMessage: String

	/**
	 * validates arguments, can be expensive to run
	 */
	fun validateContents()

	fun bindOutput(output: Consumer<OUTPUT>) {
		outputRef = output
	}

	var outputRef: Consumer<OUTPUT>?
	fun acceptResolutionOutput(output: OUTPUT) {
		outputRef?.accept(output)
	}
}

/**
 * causes a dependency to try to mount after others
 */
class Yields(override val feature: Feature) : Dependency<Any?, Boolean> {
	override fun resolves(args: Boolean): Pair<Boolean, Any?> = Pair(args, null)

	override val failures: Collection<String> = emptyList();
	override val dependencyResolutionFailureMessage: String = "failed to yield";
	override fun validateContents() {}

	override var outputRef: Consumer<Any?>? = null
}

/**
 * Resolves if this successfully yields to at least one of all [features], returns all features which are of the specified classes
 */
class YieldsTo(override val feature: Feature, private vararg val features: Class<out Feature>) : Dependency<Collection<Feature>, Pair<Boolean, Collection<Feature>>> {
	override fun resolves(args: Pair<Boolean, Collection<Feature>>): Pair<Boolean, Collection<Feature>> {
		if (args.first) {
			val result = mutableSetOf<Feature>()
			args.second.forEach {
				if (it::class.java in features) result.add(it)
			}
			val classResult = result.map { it::class.java }.toSet()
			var output = true
			features.forEach {
				val found = it in classResult
				output = output and found
				if (!found) failures.add(it.simpleName)
			}
			return output to result
		}
		return false to emptySet()
	}

	override val failures: MutableList<String> = mutableListOf()

	override val dependencyResolutionFailureMessage: String = "failed to yield to"
	override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Collection<Feature>>? = null
}

abstract class FlagDependency(override val feature: Feature, protected vararg val flags: Class<out Annotation>) : Dependency<Collection<Annotation>, Collection<Annotation>> {
	final override fun validateContents() {
		if (
				TeleOp::class.java in flags ||
				Autonomous::class.java in flags ||
				Disabled::class.java in flags
		)
			throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: annotations that are used as part of the base sdk are illegal flag dependency arguments")
	}

	override var outputRef: Consumer<Collection<Annotation>>? = null
}

abstract class FeatureDependency(override val feature: Feature, protected vararg val features: Class<out Feature>) : Dependency<Collection<Feature>, Collection<Feature>> {
	final override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Collection<Feature>>? = null
}

/**
 * resolves if the flags on the OpMode contain at least one of these Flags
 */
class IncludesAtLeastOneOf(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, Set<Annotation>> {
		var outcome = false
		val result = mutableSetOf<Annotation>()
		args.forEach {
			if(it::class.java in flags)  {
				outcome = true
				result.add(it)
			}
		}
		if (outcome) return true to result
		return false to args.toSet()
	}

	override val failures: Collection<String> = flags.map { it.simpleName }
	override val dependencyResolutionFailureMessage = "found flags did not include at least one of the following"
}

/**
 * resolves if the flags on the OpMode contain none of these Flags
 */
class ExcludesFlags(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, Set<Annotation>> {
		failures.clear()
		var outcome = true
		args.forEach {
			if(it::class.java in flags)  {
				outcome = false
				failures.add(it::class.java.simpleName)
			}
		}
		return outcome to emptySet()
	}

	override val failures: MutableSet<String> = mutableSetOf()
	override val dependencyResolutionFailureMessage  = "found flags included excluded flags"
}

/**
 * resolves if the flags on the OpMode contain exactly one of these Flags
 */
class IncludesExactlyOneOf(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, List<Annotation>> {
		failures.clear()
		val result = mutableListOf<Annotation>()
		var outcome = false
		args.forEach {
			if(it::class.java in flags) {
				if(result.getOrNull(0) == null) {
					outcome = true
					result[0] = it
				}
				else {
					outcome = false
					failures.add(it::class.java.simpleName)
				}
			}
		}
		return outcome to result
	}

	override val failures: MutableSet<String> = mutableSetOf();
	override val dependencyResolutionFailureMessage: String
		get() {
			if(failures.isEmpty()) {
				failures.addAll(flags.map { it.simpleName })
				return "found flags did not include exactly one of the following"
			}
			return "found excess flags"
		}
}

/**
 * ensures one of the arguments are attached and processed first before this, otherwise doesn't attach it, returns the first one found
 */
class DependsOnOneOf(override val feature: Feature, private vararg val features: Class<out Feature>) : Dependency<Feature?, Collection<Feature>> {
	override fun resolves(args: Collection<Feature>): Pair<Boolean, Feature?> {
		args.forEach {
			if (it::class.java in features) return true to it
		}
		return false to null
	}

	override val failures: Collection<String> = features.map { it.simpleName }
	override val dependencyResolutionFailureMessage = "found features did not include at least one of the following types"
	override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Feature?>? = null
}

/**
 * ensures none of the arguments are attached
 */
class MutuallyExclusiveWith(feature: Feature, vararg features: Class<out Feature>) : FeatureDependency(feature, *features) {
	override fun resolves(args: Collection<Feature>): Pair<Boolean, Collection<Feature>> {
		failures.clear()
		var outcome = true
		args.forEach {
			if(it::class.java in features)  {
				outcome = false
				failures.add(it::class.java.simpleName)
			}
		}
		return outcome to emptySet()
	}

	override val failures: MutableSet<String> = mutableSetOf()
	override val dependencyResolutionFailureMessage = "excluded features were activated"
}

