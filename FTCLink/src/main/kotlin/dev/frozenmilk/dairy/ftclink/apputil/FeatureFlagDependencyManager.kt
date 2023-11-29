package dev.frozenmilk.dairy.ftclink.apputil

/**
 * stores a set of Dependencies
 *
 * a feature is enabled if all of its dependencies resolve
 */
class FeatureFlagDependencyManager(private val feature: Listener, vararg dependencies: Dependency) {
	private val dependencies = setOf(*dependencies)

	/**
	 * returns true if all dependencies resolve
	 */
	fun enabled(flags: Set<Class<out Annotation>>): Boolean {
		var result = true
		dependencies.forEach { dependency ->
			result = result and dependency.resolves(flags)
		}
		return result
	}

	/**
	 * throws an error if the dependencies doesn't resolve that contains some helpful diagnostic information
	 * @see[DependencyResolutionFailureException]
	 */
	fun resolveOrError(flags: Set<Class<out Annotation>>) {
		dependencies.forEach { dependency ->
			dependency.resolvesOrError(feature, flags)
		}
	}
}

class DependencyResolutionFailureException(feature: Listener, message: String) : RuntimeException("Failed to resolve dependencies for ${feature.javaClass.simpleName} as found flags " + message)
sealed interface Dependency {
	/**
	 * returns true if this resolves against the found arguments
	 */
	fun resolves(flags: Set<Class<out Annotation>>): Boolean

	/**
	 * throws an error if the dependency doesn't resolve that contains some helpful diagnostic information
	 * @see[DependencyResolutionFailureException]
	 */
	fun resolvesOrError(feature: Listener, flags: Set<Class<out Annotation>>) {
		if (!resolves(flags)) throw DependencyResolutionFailureException(feature, dependencyResolutionFailureMessage)
	}

	val dependencyResolutionFailureMessage: String
}

/**
 * resolves if the flags on the OpMode contain at least one of these Flags
 */
class IncludesAtLeastOneOf(private vararg val flags: Class<out Annotation>) : Dependency {
	override fun resolves(flags: Set<Class<out Annotation>>): Boolean {
		return this.flags.any {
			it in flags
		}
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("did not include at least one of the following: ")
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
class Excludes(private vararg val flags: Class<out Annotation>) : Dependency {
	override fun resolves(flags: Set<Class<out Annotation>>): Boolean {
		return this.flags.all {
			it !in flags
		}
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("included an excluded flag from the following: ")
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
class IncludesExactlyOneOf(private vararg val flags: Class<out Annotation>) : Dependency {
	override fun resolves(flags: Set<Class<out Annotation>>): Boolean {
		return this.flags.filter {
			it in flags
		}.size != 1
	}

	override val dependencyResolutionFailureMessage by lazy {
		buildString {
			append("did not include exactly one of the following: ")
			flags.forEachIndexed { index, it ->
				append(it)
				if (index < flags.size) append(", ")
				else append(".")
			}
		}
	}
}