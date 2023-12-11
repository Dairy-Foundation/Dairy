package dev.frozenmilk.dairy.core.dependencyresolution.dependencies

import dev.frozenmilk.dairy.core.Feature

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