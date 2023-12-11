package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.IncludesExactlyOneOf
import java.util.function.Consumer

class IndividualFlagBoundDependencySet(dependencySet: DependencySet) : DependencySet(dependencySet.feature, dependencySet) {
	fun bindOutputTo(outputConsumer: Consumer<Annotation>) : DependencySet {
		(last() as IncludesExactlyOneOf).bindOutput(outputConsumer)
		return this
	}
}