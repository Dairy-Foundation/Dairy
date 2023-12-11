package dev.frozenmilk.dairy.calcified.gamepad

import org.jetbrains.annotations.Contract
import java.util.function.DoubleSupplier

class Conditional internal constructor(private val doubleSupplier: DoubleSupplier) {
	private val domainCheckers: ArrayList<(Double) -> Boolean> = ArrayList(1)
	private var domainClosureBuilder: DomainClosureBuilder = DomainClosureBuilder()
	private var previousOperationType: OperationType? = null

	/**
	 * @return self, for chaining
	 */
	fun lessThan(value: Double): Conditional {
		handleBuildState(OperationType.LESS, Inclusivity.NOT_INCLUSIVE, value)
		domainClosureBuilder = domainClosureBuilder.lessThan(value)
		return this
	}

	/**
	 * @return self, for chaining
	 */
	fun lessThanEqualTo(value: Double): Conditional {
		handleBuildState(OperationType.LESS, Inclusivity.INCLUSIVE, value)
		domainClosureBuilder = domainClosureBuilder.lessThanEqualTo(value)
		return this
	}

	/**
	 * @return self, for chaining
	 */
	fun greaterThan(value: Double): Conditional {
		handleBuildState(OperationType.GREATER, Inclusivity.NOT_INCLUSIVE, value)
		domainClosureBuilder = domainClosureBuilder.greaterThan(value)
		return this
	}
	// when we do a new operation, check to see if it can form a valid closure with the previous operation, if so, perform the closure union, else, close the previous closure and add this one in
	// closes if upper > lower
	// only need to check if we currently already have one domain set
	/**
	 * @return self, for chaining
	 */
	fun greaterThanEqualTo(value: Double): Conditional {
		handleBuildState(OperationType.GREATER, Inclusivity.INCLUSIVE, value)
		domainClosureBuilder = domainClosureBuilder.greaterThanEqualTo(value)
		return this
	}

	fun bind(): EnhancedBooleanSupplier {
		if (domainClosureBuilder.lower != Double.NEGATIVE_INFINITY || domainClosureBuilder.upper != Double.POSITIVE_INFINITY) {
			domainCheckers.add(domainClosureBuilder.build())
		}

		// todo simplify domain checkers by checking their extremes and seeing if one entirely contains another or if two could be merged?
		// doesn't matter for the moment, but very plausible for later
		return EnhancedBooleanSupplier {
			var result = false
			for (domainChecker in domainCheckers) {
				result = result or domainChecker.invoke(doubleSupplier.asDouble)
			}
			result
		}
	}

	// we should perform a build if:
	// * we already performed an operation of this sign (less / greater)
	// * we already have one value loaded in there AND:
	// * the new value doesn't close, so we actually want inverse values, which we achieve by building the previous value and letting the user continue to cook
	// * OTHERWISE: if the new value DOES close, we add it and then run a build
	private fun handleBuildState(operationType: OperationType, inclusivity: Inclusivity, newValue: Double) {
		if (previousOperationType == operationType || operationType == OperationType.LESS && (newValue < domainClosureBuilder.lower && inclusivity.isInclusive || newValue <= domainClosureBuilder.lower && !inclusivity.isInclusive) || operationType == OperationType.GREATER && (domainClosureBuilder.upper < newValue && inclusivity.isInclusive || domainClosureBuilder.upper <= newValue && !inclusivity.isInclusive)) {
			domainCheckers.add(domainClosureBuilder.build())
			domainClosureBuilder = DomainClosureBuilder()
		}
		previousOperationType = operationType
	}
}
private enum class OperationType {
	LESS,
	GREATER
}

internal enum class Inclusivity(val isInclusive: Boolean) {
	INCLUSIVE(true),
	NOT_INCLUSIVE(false)

}

class DomainClosureBuilder internal constructor(internal val lower: Double = Double.NEGATIVE_INFINITY, private val lowerInclusive: Inclusivity = Inclusivity.INCLUSIVE, internal val upper: Double = Double.POSITIVE_INFINITY, private val upperInclusive: Inclusivity = Inclusivity.INCLUSIVE) {
	@Contract("_ -> new")
	fun lessThan(value: Double): DomainClosureBuilder {
		return DomainClosureBuilder(lower, lowerInclusive, value, Inclusivity.NOT_INCLUSIVE)
	}

	@Contract("_ -> new")
	fun lessThanEqualTo(value: Double): DomainClosureBuilder {
		return DomainClosureBuilder(lower, lowerInclusive, value, Inclusivity.INCLUSIVE)
	}

	@Contract("_ -> new")
	fun greaterThan(value: Double): DomainClosureBuilder {
		return DomainClosureBuilder(value, Inclusivity.NOT_INCLUSIVE, upper, upperInclusive)
	}

	@Contract("_ -> new")
	fun greaterThanEqualTo(value: Double): DomainClosureBuilder {
		return DomainClosureBuilder(value, Inclusivity.INCLUSIVE, upper, upperInclusive)
	}

	@Contract(pure = true)
	fun build(): (Double) -> Boolean {
		return { value: Double ->
			var result = value > lower && value < upper
			result = result or (lowerInclusive.isInclusive && value == lower)
			result = result or (upperInclusive.isInclusive && value == upper)
			result
		}
	}
}