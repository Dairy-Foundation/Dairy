package dev.frozenmilk.dairy.core.dependencyresolution.dependencyset

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.YieldsTo
import java.util.function.Consumer

class YieldsToFeatureBoundDependencySet(dependencySet: DependencySet) : DependencySet(dependencySet.feature, dependencySet) {
	fun bindOutputTo(outputConsumer: Consumer<Collection<Feature>>) : DependencySet {
		(last() as YieldsTo).bindOutput(outputConsumer)
		return this
	}
}