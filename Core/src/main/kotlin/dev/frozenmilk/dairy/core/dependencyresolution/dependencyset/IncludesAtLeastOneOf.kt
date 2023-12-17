package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.FlagDependency

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