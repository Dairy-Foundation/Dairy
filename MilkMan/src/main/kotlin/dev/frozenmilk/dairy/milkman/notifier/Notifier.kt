package dev.frozenmilk.dairy.milkman

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.cell.LateInitCell

class Notifier() : Feature {
	val wsdCell = LateInitCell<Collection<Feature>>()
	override val dependencies: Set<Dependency<*, *>> = DependencySet(this).yieldsTo(MilkManWSD::class.java).bindOutputTo(wsdCell)
	val 
}