package dev.frozenmilk.dairy.ftclink.apputil

/**
 * stores a set of FlagConditions
 * A feature flag is enabled if the flags provided meet the requirements of all flag conditions
 */
class FeatureFlagDependencyManager(vararg flagConditions: FlagCondition) {
	private val requiredFlags: Set<FlagCondition> = setOf(*flagConditions)

	/**
	 * returns true if each flag condition is satisfied
	 */
	fun enabled(flags: Set<Class<out Annotation>>): Boolean {
		var result = true
		requiredFlags.forEach { option ->
			result = result and option.satisfied(flags)
		}
		return result
	}
}

/**
 * stores a set of flags
 * the condition is satisfied if any of the flags passed in to the satisfied method match any of the flags contained within
 */
class FlagCondition(vararg val flags: Class<out Annotation>) {
	/**
	 * the condition is satisfied if any of the flags passed in match any of the flags contained within this
	 */
	fun satisfied(flags: Set<Class<out Annotation>>): Boolean {
		return this.flags.any {
			it in flags
		}
	}
}