package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.FlagDependency
import java.util.function.Consumer

class FlagBoundDependencySet(dependencySet: DependencySet) : DependencySet(dependencySet.feature, dependencySet) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Annotation>>): DependencySet {
		(last() as FlagDependency).bindOutput(outputConsumer)
		return this
	}
}