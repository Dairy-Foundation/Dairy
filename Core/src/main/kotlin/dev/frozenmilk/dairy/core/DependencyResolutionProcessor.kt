package dev.frozenmilk.dairy.core

/**
 * internal
 */
fun resolveDependencies(unresolvedFeatures: MutableSet<Feature>, currentlyActiveFeatures: Set<Feature>, featureFlags: Set<Annotation>): Map<Feature, Set<FeatureDependencyResolutionFailureException>> {
	val resolved = mutableMapOf<Feature, MutableSet<FeatureDependencyResolutionFailureException>>()
	var notLocked = true
	var yielding = false

	while (notLocked || yielding) {
		val unresolvedSize = unresolvedFeatures.size
		unresolvedFeatures.forEach { feature ->
			var resolves = feature.dependencies.isNotEmpty() // if there are no dependencies, it won't be allowed to mount
			val exceptions = mutableSetOf<FeatureDependencyResolutionFailureException>()
			feature.dependencies.forEach {
				resolves = when (it) {
					is FlagDependency -> {
						try {
							val resolutionResult = it.resolvesOrError(featureFlags)
							if(resolutionResult.first) it.acceptResolutionOutput(resolutionResult.second)
							resolves and resolutionResult.first
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is FeatureDependency -> {
						try {
							val newResolutionResult = it.resolvesOrError(resolved.keys)
							val currentResolutionResult = it.resolvesOrError(currentlyActiveFeatures)
							if(newResolutionResult.first or currentResolutionResult.first) it.acceptResolutionOutput(newResolutionResult.second.plus(currentResolutionResult.second));
							resolves and (newResolutionResult.first or currentResolutionResult.first)
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is DependsOnOneOf -> {
						try {
							val newResolutionResult = it.resolvesOrError(resolved.keys)
							val currentResolutionResult = it.resolvesOrError(currentlyActiveFeatures)
							if (newResolutionResult.first or currentResolutionResult.first) it.acceptResolutionOutput(newResolutionResult.second.plus(currentResolutionResult.second).firstOrNull());
							resolves and (newResolutionResult.first or currentResolutionResult.first)
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is Yields -> {
						try {
							val resolutionResult = it.resolvesOrError(yielding)
							resolves and resolutionResult.first
						}
						catch (e: FeatureDependencyResolutionFailureException) {
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
			}
		}

		// if we didn't manage to resolve anything new, we have a deadlock, and we should give up, and move on
		notLocked = unresolvedFeatures.size != unresolvedSize
		yielding = (!notLocked && !yielding)
	}

	// the remaining features caused a deadlock, so we'll add a one off exception for them
	unresolvedFeatures.forEach {
		resolved[it]!!.add(FeatureDependencyResolutionFailureException(it, "attempts to resolve this dependency resulted in a deadlock", emptySet()))
	}

	unresolvedFeatures.clear()

	return resolved
}