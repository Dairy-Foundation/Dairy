package dev.frozenmilk.dairy.core

/**
 * todo
 */
fun resolveDependenciesMap(unresolvedFeatures: Collection<Feature>, currentlyActiveFeatures: Collection<Feature>, featureFlags: Collection<Annotation>): Map<Feature, Set<FeatureDependencyResolutionFailureException>> {
	val unresolvedFeatureMap: MutableCollection<ResolutionPair> = unresolvedFeatures.map { ResolutionPair(it) }.toMutableList()
	val resolved = linkedMapOf<Feature, Set<FeatureDependencyResolutionFailureException>>()
	var notLocked = true
	var yielding = false

	while (notLocked || yielding) {
		val unresolvedSize = unresolvedFeatureMap.size
		unresolvedFeatureMap.forEach { resolutionPair ->
			resolutionPair.resolves = resolutionPair.feature.dependencies.isNotEmpty() // if there are no dependencies, it won't be allowed to mount
			val exceptions = mutableSetOf<FeatureDependencyResolutionFailureException>()
			resolutionPair.feature.dependencies.forEach {
				resolutionPair.resolves = when (it) {
					is FlagDependency -> {
						try {
							val resolutionResult = it.resolvesOrError(featureFlags)
							if(resolutionResult.first) it.acceptResolutionOutput(resolutionResult.second)
							resolutionPair.resolves and resolutionResult.first
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
							resolutionPair.resolves and (newResolutionResult.first or currentResolutionResult.first)
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is DependsOnOneOf -> {
						try {
							val newResolutionResult = it.resolvesOrError(resolved.keys)
							val currentResolutionResult = it.resolvesOrError(currentlyActiveFeatures)
							if (newResolutionResult.first or currentResolutionResult.first) it.acceptResolutionOutput(currentResolutionResult.second ?: newResolutionResult.second);
							resolutionPair.resolves and (newResolutionResult.first or currentResolutionResult.first)
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is Yields -> {
						try {
							val resolutionResult = it.resolvesOrError(yielding)
							resolutionPair.resolves and resolutionResult.first
						}
						catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is YieldsTo -> {
						try {
							val newResolutionResult = it.resolvesOrError(yielding to resolved.keys)
							val currentResolutionResult = it.resolvesOrError(yielding to currentlyActiveFeatures)
							if (newResolutionResult.first or currentResolutionResult.first) it.acceptResolutionOutput(newResolutionResult.second.plus(currentResolutionResult.second));
							resolutionPair.resolves and (newResolutionResult.first or currentResolutionResult.first)
						}
						catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}

					is IncludesExactlyOneOf -> {
						try {
							val resolutionResult = it.resolvesOrError(featureFlags)
							if(resolutionResult.first) it.acceptResolutionOutput(resolutionResult.second)
							resolutionPair.resolves and resolutionResult.first
						} catch (e: FeatureDependencyResolutionFailureException) {
							exceptions.add(e)
							false
						}
					}
				}
			}
			resolved.putIfAbsent(resolutionPair.feature, mutableSetOf())
			resolved[resolutionPair.feature] = resolved[resolutionPair.feature]!!.plus(exceptions)
		}

		val iter = unresolvedFeatureMap.iterator()
		while (iter.hasNext()) {
			val resolutionPair = iter.next()
			if(resolutionPair.resolves) {
				iter.remove()
				// clear the issues found, as feature dependencies may have caused a feature to fail the first round, but pass in a later one
				resolved[resolutionPair.feature] = mutableSetOf()
			}
		}

		// if we didn't manage to resolve anything new, we have a deadlock, and we should give up, and move on
		notLocked = unresolvedFeatureMap.size != unresolvedSize
		yielding = (!notLocked && !yielding)
	}

	// the remaining features caused a deadlock, so we'll add a one off exception for them
	unresolvedFeatureMap.forEach {
		resolved[it.feature] = resolved[it.feature]!!.plus(FeatureDependencyResolutionFailureException(it.feature, "attempts to resolve this dependency resulted in a deadlock", emptySet()))
	}

	unresolvedFeatureMap.clear()

	return resolved
}

/**
 * todo
 */
fun resolveDependenciesOrderedList(unresolvedFeatures: Collection<Feature>, currentlyActiveFeatures: Collection<Feature>, featureFlags: Collection<Annotation>): List<Pair<Feature, Set<FeatureDependencyResolutionFailureException>>> {
	return resolveDependenciesMap(unresolvedFeatures, currentlyActiveFeatures, featureFlags).toList().reversed()
}

internal class ResolutionPair(val feature: Feature, var resolves: Boolean = false)