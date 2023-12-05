package dev.frozenmilk.dairy.core

/**
 * internal
 */
fun resolveDependencies(unresolvedFeatures: MutableSet<Feature>, currentlyActiveFeatures: Set<Feature>, featureFlags: Map<Class<out Annotation>, Annotation>): Map<Feature, Set<DependencyResolutionFailureException>> {
	val resolved = mutableMapOf<Feature, MutableSet<DependencyResolutionFailureException>>()
	var notLocked = true

	while (notLocked) {
		val unresolvedSize = unresolvedFeatures.size
		unresolvedFeatures.forEach { feature ->
			var resolves = feature.dependencies.isNotEmpty() // if there are no dependencies, it won't be allowed to mount
			val exceptions = mutableSetOf<DependencyResolutionFailureException>()
			feature.dependencies.forEach {
				resolves = when (it) {
					is FlagDependency -> {
						try {
							resolves and it.resolvesOrError(featureFlags.keys)
						} catch (e: DependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is FeatureDependency -> {
						try {
							resolves and (it.resolvesOrError(resolved.keys) or it.resolvesOrError(currentlyActiveFeatures))
						} catch (e: DependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}
				}
			}
			resolved.putIfAbsent(feature, mutableSetOf())
			resolved[feature]!!.addAll(exceptions)
			if (resolves) {
				unresolvedFeatures.remove(feature)
				// clear the issues found, as feature dependencies may have caused a feature to fail the first round, but pass in a later one
				resolved[feature] = mutableSetOf()
//				feature.dependencies.forEach {
//					// todo return the features and flags to the dependencies
//					it.acceptResolutionOutput()
//				}
			}
		}

		// if we didn't manage to resolve anything new, we have a deadlock, and we should give up, and move on
		notLocked = unresolvedFeatures.size != unresolvedSize
	}

	// the remaining features caused a deadlock, so we'll add a one off exception for them
	unresolvedFeatures.forEach {
		resolved[it]!!.add(DependencyResolutionFailureException(it, "attempts to resolve this dependency resulted in a deadlock"))
	}

	unresolvedFeatures.clear()

	return resolved
}