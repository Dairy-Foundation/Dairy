package dev.frozenmilk.dairy.core.dependencyresolution

import dev.frozenmilk.dairy.core.Feature
import java.util.Objects

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
