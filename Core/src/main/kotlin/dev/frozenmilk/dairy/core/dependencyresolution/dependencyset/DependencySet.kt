package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.DependsOnOneOf
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.ExcludesFlags
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.FlagDependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.IncludesExactlyOneOf
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.MutuallyExclusiveWith
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Yields
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.YieldsTo

/**
 * stores a set of dependency resolution rules for a feature
 *
 * a feature is enabled if all of its dependencies resolve
 */
open class DependencySet internal constructor(internal val feature: Feature, dependencies: Set<Dependency<*, *>>) : Set<Dependency<*, *>> by dependencies {
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
		return FlagBoundDependencySet(withDependency(IncludesAtLeastOneOf(feature, *flags)))
	}

	/**
	 * non-mutating
	 *
	 * @see [IncludesExactlyOneOf]
	 */
	fun includesExactlyOneOf(vararg flags: Class<out Annotation>): IndividualFlagBoundDependencySet {
		return IndividualFlagBoundDependencySet(withDependency(IncludesExactlyOneOf(feature, *flags)))
	}

	/**
	 * non-mutating
	 *
	 * @see [ExcludesFlags]
	 */
	fun excludesFlags(vararg flags: Class<out Annotation>): FlagBoundDependencySet {
		return FlagBoundDependencySet(withDependency(ExcludesFlags(feature, *flags)))
	}

	/**
	 * non-mutating
	 *
	 * @see [DependsOnOneOf]
	 */
	fun dependsOnOneOf(vararg features: Class<out Feature>): SingleFeatureDependencySet {
		return SingleFeatureDependencySet(withDependency(DependsOnOneOf(feature, *features)))
	}

	/**
	 * non-mutating
	 *
	 * @see [DependsOnOneOf]
	 */
	fun mutuallyExclusiveWith(vararg features: Class<out Feature>): FeatureBoundDependencySet {
		return FeatureBoundDependencySet(withDependency(MutuallyExclusiveWith(feature, *features)))
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
		return YieldsToFeatureBoundDependencySet(withDependency(YieldsTo(feature, *features)))
	}
}

/**
 * resolves if the flags on the OpMode contain at least one of these Flags
 */
class IncludesAtLeastOneOf(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, Set<Annotation>> {
		var outcome = false
		val result = mutableSetOf<Annotation>()
		args.forEach {
			if(it.annotationClass.java in flags)  {
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

