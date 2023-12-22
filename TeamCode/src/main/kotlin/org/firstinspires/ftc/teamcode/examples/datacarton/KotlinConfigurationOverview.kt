package org.firstinspires.ftc.teamcode.examples.datacarton

import collections.annotatedtargets.GroupedData
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import datacarton.DataBlock
import datacarton.DataBlockRender
import datacarton.DataCarton
import datacarton.MessageBoard
import datacarton.MessageBoardRender
import datacarton.Render
import datacarton.RenderOrder
import datacarton.processors.DataPackageProcessor
import datacarton.processors.LogPublicationProcessor
import datacarton.processors.TelemetryPublicationProcessor
import dev.frozenmilk.dairy.core.FeatureRegistrar

@DataCarton.Attach( // attaches the Calcified feature
		autoUpdate = true // this is the setting for the feature that we can set
		// true is the default, but if you're a more advanced user you may want to make use of this
)
// @DairyCore
// can also be used to activate all dairy library features, but doesn't allow settings,
// also, if @DairyCore is present it will clash with the @DataCarton.Attach annotation

// the OpMode also needs to implement GroupedData
class KotlinOverview : OpMode(), GroupedData {
	init {
		// this ensures that DataCarton is attached,
		// if it failed for some reason, then it will spit out a helpful error describing why
		// what you asked for wasn't successfully attached

		// if this line isn't here, the first time you run an opmode with DataCarton in it it might crash,
		// and then work after that, due to the way classes are loaded in java,
		// so this line is advised even if you know that everything should be fine
		FeatureRegistrar.checkFeatures(this, DataCarton)
	}
	override fun init() {
		// do your regular init stuff

		// note you should only run one of the following, these are provided for the purpose of demonstration

		// super simple default set up for telemetry
		DataCarton.initFromTelemetry(telemetry)

		// set up for telemetry and customise the default display format
		DataCarton.initFromTelemetry(
				telemetry,
				RenderOrder.BLOCK_FIRST, // a different Render order can be picked
		)

		// set up for telemetry, customise the default display format, and specify other outputs
		DataCarton.initFromTelemetry(
				telemetry,
				RenderOrder.BLOCK_FIRST_MESSAGE_FORWARD,
				LogPublicationProcessor() // varargs attach additional processors
		)

		// a default init without telemetry
		DataCarton.initWithDefaultPackageProcessors()

		// a default init without telemetry, customise the default display format
		DataCarton.initWithDefaultPackageProcessors(RenderOrder.MESSAGE_FIRST)

		// a default init without telemetry, varargs attach additional processors
		DataCarton.initWithDefaultPackageProcessors(LogPublicationProcessor())

		// a default init without telemetry, customise the default display format, varargs attach additional processors
		DataCarton.initWithDefaultPackageProcessors(RenderOrder.BLOCK_FIRST_MESSAGE_FORWARD, LogPublicationProcessor())

		// this doesn't need to be done if one of the above init methods was used
		// standalone adding a package processor
		DataCarton.packageProcessors.add(DataPackageProcessor)

		// either this can be done, or it can be added via the varargs in one of the above init methods
		// standalone adding a publication processor
		DataCarton.publicationProcessors.add(LogPublicationProcessor())
		DataCarton.publicationProcessors.add(TelemetryPublicationProcessor(telemetry))

		// customising the settings

		// set the default display format
		DataCarton.defaultRenderOrder = RenderOrder.DEFAULT_MAPPING

		DataCarton
				// configure settings for the group named container
				.configureFor("container")
				// set a pre-defined render order for them
				.with(RenderOrder.MESSAGE_FIRST)
				// configure settings for the group named flatContainer
				.configureFor("flatContainer")
				// set to use the default data block followed by the default reverse message board
				.with(Render.DEFAULT_DATA_BLOCK)
				.with(Render.DEFAULT_REVERSE_MESSAGE_BOARD)
				// configure settings for the group named custom named group
				.configureFor("custom named group")
				// specify settings for the MessageBoardRender
				// reversed means messages are displayed oldest to newest from top to bottom
				// not reversed means messages are displayed newest to oldest top to bottom
				// the len determines how many messages are shown before the oldest is removed
				// usually the minLen doesn't need to be specified
				// but if the length is being determined programmatically
				// the setting may become important
				.with(MessageBoardRender(false, 12, 5))
				// there are no settings to specify for the data block render
				.with(DataBlockRender())

		// when declaring settings by hand, leaving out an aspect (either the datablock or the messageboard)
		// will result in that component not showing up in any outputs
		// which makes it super easy to quickly disable a section of the outputs

		// We've seen a bunch of settings, so we'll break down

		// Renders

		// the default data block
		// stores captions and values (like telemetry.addData(caption, value))
		// there are no settings, and its all pretty chill
		Render.DEFAULT_DATA_BLOCK

		// the default message board
		// stores messages with a timed caption
		// has a length of 5
		// displays messages newest to oldest, top to bottom
		Render.DEFAULT_MESSAGE_BOARD
		// same as above, but
		// displays messages oldest to newest, top to bottom
		Render.DEFAULT_REVERSE_MESSAGE_BOARD


		// Prebuilt RenderOrders:

		// the default mapping
		// 
		RenderOrder.DEFAULT_MAPPING
		// which is the same as:
		RenderOrder.BLOCK_FIRST

		// lets DataCarton know that you're done configuring,
		// and it can go looking for data now
		// this needs to be called after you've set up everything in your opmode, as its expensive, and can't search null values
		DataCarton.packageData(this)

		// if auto update is off, you can update DataCarton manually like so:
		DataCarton.update()
	}

	override fun loop() {
		TODO("Not yet implemented")
	}
}