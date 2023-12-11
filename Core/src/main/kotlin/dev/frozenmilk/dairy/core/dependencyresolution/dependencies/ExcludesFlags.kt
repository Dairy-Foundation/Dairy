package dev.frozenmilk.dairy.core.dependencyresolution.dependencies

import dev.frozenmilk.dairy.core.Feature

/**
 * resolves if the flags on the OpMode contain none of these Flags
 */
class ExcludesFlags(feature: Feature, vararg flags: Class<out Annotation>) : FlagDependency(feature, *flags) {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, Set<Annotation>> {
		failures.clear()
		var outcome = true
		args.forEach {
			if(it.annotationClass.java in flags)  {
				outcome = false
				failures.add(it.annotationClass.java.simpleName)
			}
		}
		return outcome to emptySet()
	}

	override val failures: MutableSet<String> = mutableSetOf()
	override val dependencyResolutionFailureMessage  = "found flags included excluded flags"
}