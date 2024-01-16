package org.firstinspires.ftc.teamcode.examples.datacarton

import datacarton.DataCarton
import datacarton.annotations.Data
import datacarton.annotations.Export
import datacarton.annotations.Import
import datacarton.annotations.Pack
import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import java.util.function.Supplier

/**
 * used in the KotlinImplementationOverview
 */
class Container {
	// this value will be found if a container is annotated with @Pack, but bundle is set to false
	@Data
	val containedValue = true

	// this value will be found if a container is annotated with @Pack, and will be bundled up by default
	// if bundle is set to true, and if the @Import annotation is also applied, if include defaults is set to true
	@Data
	// todo the export annotation is currently under development, and its singular setting is currently equivalent to removing it
	// its being left on there for the sake of future work being done on it
	@Export
	val exportedValue = "DataCarton"

	// this won't be found by DataCarton unless the @Import annotation is used to extract it by force
	val unfound = null

	// this will cause the contents of the supplier to be found by DataCarton
	// this is a special behaviour of suppliers
	// which means it also works for cells
	// the value "transparent" will be found, with the caption "supplier"
	@Data
	@Export
	val supplier = Supplier { "transparent" }

	// as shown here, the string "cell value" will be recorded
	// @Pack can also be used if the contents of the supplier are complex
	// the value "cell value" will be found, with the caption "cell"
	@Data
	val cell = OpModeLazyCell {
		"cell value"
	}

	@Pack
	// todo at the moment, the rest of the Dairy libraries aren't set up to use DataCarton
	// but in the future, this could make it super easy to enable a ton of information about a motor or encoder being displayed
	// this prevents all the information from being dumped, and makes sure we only get power
	@Import(includeDefaults = false, dataMethods = ["getPower"])
	val motor = Calcified.controlHub.getMotor(0)

	fun messagingMethod() {
		// this publishes a message to the message board we set up in the OpMode every time it gets run
		DataCarton.publishMessage("opmode messages", "container", "method was invoked")
	}
}