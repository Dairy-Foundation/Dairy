package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.FeatureDependency
import java.util.function.Consumer

class FeatureBoundDependencySet(dependencySet: DependencySet) : DependencySet(dependencySet.feature, dependencySet) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Feature>>): DependencySet {
		(last() as FeatureDependency).bindOutput(outputConsumer)
		return this
	}
}