package org.firstinspires.ftc.teamcode.examples.datacarton

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import datacarton.DataCarton
import datacarton.Render
import datacarton.annotations.Data
import datacarton.annotations.Import
import datacarton.annotations.Pack
import datacarton.processors.DataLogPublicationProcessor
import datacarton.processors.MessageLogPublicationProcessor
import dev.frozenmilk.dairy.core.FeatureRegistrar

@TeleOp
@DataCarton.Attach( // attaches the Calcified feature
		autoUpdate = true // this is the setting for the feature that we can set
		// true is the default, but if you're a more advanced user you may want to make use of this
)
// @DairyCore
// can also be used to activate all dairy library features, but doesn't allow settings,
// also, if @DairyCore is present it will clash with the @DataCarton.Attach annotation

class KotlinImplementationOverview : OpMode() {

	// the @Pack annotation can be used on val
	@Pack
	private val container = Container()

	// the @Pack annotation can also be used on the getter of a var
	// note that this causes issues in kotlin when the var is private
	// this can be circumvented by writing the getter for the var by hand and then annotating that
	// or by using a cell wrapper to achieve interior mutability (see below)
	@get:Pack
	var varContainer = Container()

	// the @Pack annotation can also be applied to a method with no parameters
	@Pack
	fun getVarContainerFunction(): Container {
		return varContainer
	}

	// breaking down @Pack:
	// @Pack tells DataCarton that this contains data further down,
	// its useful for working with nested data, where the data you actually want is further down in the class
	@Pack(
			// specifying the group parameter will allow this group of data to be renamed and placed under another heading
			// otherwise, this would go under the name "getContainer" by default
			group = "a specific data heading",
			// bundle being set to true lets DataCarton that you want to pull data out of here and bring it to this level
			// in this case, the contents of this container get bundled under the heading "KotlinImplementationOverview"
			// and the group that we just set gets ignored
			// by default, bundle brings up a specific set of values
			bundle = true
	)
	// the @Import annotation can be used in combination with the @Pack annotation
	// it comes in useful when you want to specify what you're extracting when bundle is set to true
	// its also useful for extracting information from classes that don't have DataCarton set up
	@Import(
			// if this is set to true, all @Pack and @Data annotated targets that also have
			// @Export will be automatically extracted when bundling
			includeDefaults = true,
			// the pack fields and methods allow you to specify the names of fields and methods that you want to apply @Pack to
			packFields = [],
			packMethods = [],
			// the data fields and methods allow you to specify the names of fields and methods that you want to apply @Data to
			dataFields = [],
			dataMethods = [],
	)
	// the features on @Import are pretty powerful, and are more suited to power users
	// we're working on improving @Import functionality to ensure that DataCarton is usable
	// for extracting and recording data from libraries that don't support it
	// note, as these features are a wip, they are most likely to cause issues atm
	fun getContainerFunc(): Container {
		return container
	}

	// The @Data annotation can be applied in all the same ways as the @Pack annotation
	// This lets DataCarton know that this is a piece of data
	// Its pretty similar to telemetry.addData("motorPosition", motorPosition);
	@Data(
			// specifying the group parameter will allow this piece of data to be renamed and placed under another heading
			// otherwise, this would go under the name "KotlinImplementationOverview" by default
			group = "a specific data heading",
			// the label is a lot like the group, but its just for this piece of data, like the caption in telemetry.addData()
			// by default this would have been "motorPosition"
			label = "motorPosition"
	)
	val motorPosition = 0.0

	// now take a look at the Container class, and see what data will be pulled out of it
	// or look here, to see a practical showing of setting up DataCarton looks like,
	// and how to send a message

	init {
		// this ensures that DataCarton is attached,
		// if it failed for some reason, then it will spit out a helpful error describing why
		// what you asked for wasn't successfully attached

		// if this line isn't here, the first time you run an OpMode with DataCarton in it it might crash,
		// and then work after that, due to the way classes are loaded in java,
		// so this line is advised even if you know that everything should be fine
		FeatureRegistrar.checkFeatures(this, DataCarton)
	}
	override fun init() {
		// all other init stuff
		// its fine to use normal telemetry here

		DataCarton.initFromTelemetry(telemetry)
		DataCarton.publicationProcessors.add(DataLogPublicationProcessor("ImplementationOpMode"))
		DataCarton.publicationProcessors.add(MessageLogPublicationProcessor("ImplementationOpMode"))

		DataCarton.configureFor("opmode messages").with(Render.DEFAULT_REVERSE_MESSAGE_BOARD)

		DataCarton.packageData(this)
		// stop using normal telemetry (DataCarton cleans it up, so don't worry about fixing things up if you did use it)
	}

	override fun loop() {
		// this shows how to publish a message to a message board
		// unfortunately, theres no easy way to enable the passing of the group of an object all the way down to it
		// so if you want these to be grouped with something else, you either need to know the group ahead of time
		// or you need to pass the group down to the class as a parameter
		// this message looks like this:
		// [<time since start of opmode>]: we are looping!
		DataCarton.publishMessage("opmode messages", "we are looping!")
		// a very real and accurate loop time
		// looks like this:
		// [<time since start of opmode>]: looptime: 10.0
		DataCarton.publishMessage("opmode messages", "looptime", 10.0)
	}
}
