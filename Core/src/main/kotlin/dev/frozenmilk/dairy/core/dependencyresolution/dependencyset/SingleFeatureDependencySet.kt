package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.DependsOnOneOf
import java.util.function.Consumer

class SingleFeatureDependencySet(dependencySet: DependencySet) : DependencySet(dependencySet.feature, dependencySet) {
	fun bindOutputTo(outputConsumer: Consumer<Feature>): DependencySet {
		(last() as DependsOnOneOf).bindOutput(outputConsumer)
		return this
	}
}