package org.firstinspires.ftc.teamcode.examples.featuredev

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LateInitCell

// Todo: in the full documentation it would be very nice to put out a quick guide to setting up and publishing a dairy core library on jitpack

// to write a feature using DairyCore is pretty simple
// the object / class just needs to implement the Feature interface
// most major features are a static object, but plenty of smaller things implement feature in order to
// make use of the automation and hooks that the system provides
// for instance, OpModeLazyCells are features, that use the system to eagerly evaluate themselves in init
object KotlinWritingAFeature : Feature {
	// this uses a cell to extract a feature, we'll come back to this later
	private val calcfiedCell = LateInitCell<Feature>()

	// the dependencies are important, we use the DependencySet() class to assist with this
	// the dependencies allow the FeatureRegistrar to determine the order in which our dependencies are attached
	override val dependencies: Set<Dependency<*, *>>
		= DependencySet(this)
				// this says that we need the @Activate annotation to activate this
			.includesExactlyOneOf(Attach::class.java)
				// this allows us to run a piece of code when this feature gets activated, and the @Activate annotation will be passed to it
			.bindOutputTo { annotation ->
				when (annotation) {
					is Attach -> {
						println("my feature activated!")
					}
					else -> {
						// this can't happen, because we only asked to find @Activate
						println("something else got here!")
					}
				}
			}
				// this says we need calcified to be attached for this to be attached
			.dependsOnOneOf(Calcified::class.java)
				// outputs can also be bound to cells, instead of pieces of code, so we can extract the value to use again later
			.bindOutputTo(calcfiedCell)
				// this lets the dependency resolver know that this feature isn't very important,
				// and will wait until nothing else can be attached before it is
				// usually this isn't put on major features like this,
				// but, for the sake of demonstration...
			.yields()

	// there are lots of dependency options here, that are fairly well documented, and hard to show without a much bigger example,
	// so explore more, to figure out what best suits your project.
	// remember, no dependency set can be empty, this will prevent your Feature from being attached,
	// and will result in unhelpful errors being thrown by its resolution process

	// this block ensures that this feature is registered as soon as it comes into existence
	// it is important that it comes after the declaration of the dependencies,
	// otherwise the dependencies won't exist when this gets registered, which will cause a silent crash,
	// which is painful to debug
	init {
		FeatureRegistrar.registerFeature(this)
	}

	// this allows us to quickly get calcified out of the extraction cell, for use later
	private val calcified: Calcified
		get() {
			return calcfiedCell.get() as Calcified
		}

	override fun preUserInitHook(opMode: Wrapper) {
		// code that runs before the user's init,
		// in this case it will also be run after Calcified's version of this gets run,
		// so we can safely assume that Calcified has been set up, and use its features

		// this feature will perform a cross-controller remap
//		calcified.gamepad1.a = calcified.gamepad1.a or calcified.gamepad2.a
	}

	override fun postUserInitHook(opMode: Wrapper) {
		// hooks are provided for before and after each bit of user code

		// Between the feature registrar and the OpModeWrapper passed to this hook,
		// some extra utilities are made easily accessible

		// the same as the OpModeWrapper being passed here, this probably isn't useful to you,
		// and it would be bad practice to use it when you have the OpModeWrapper
		FeatureRegistrar.activeOpModeWrapper // but it exists none-the-less
		FeatureRegistrar.opModeActive // if an OpMode is currently active

		opMode.opModeType // teleop | autonomous | none

		// the OpModeWapper also provides access to all the parts of an OpMode you might normally access
		opMode.opMode.telemetry // the telemetry
		opMode.opMode.hardwareMap // the hardwareMap
	}

	override fun preUserInitLoopHook(opMode: Wrapper) {
	}

	override fun postUserInitLoopHook(opMode: Wrapper) {
	}

	override fun preUserStartHook(opMode: Wrapper) {
	}

	override fun postUserStartHook(opMode: Wrapper) {
	}

	override fun preUserLoopHook(opMode: Wrapper) {
	}

	override fun postUserLoopHook(opMode: Wrapper) {
	}

	override fun preUserStopHook(opMode: Wrapper) {
	}

	override fun postUserStopHook(opMode: Wrapper) {
		// some features (not this one) might want to automatically deregister themselves after the OpMode
		// while this isn't really necessary, as features are held weakly, and will disappear if the user doesn't hold onto them

		// this would be terrible practice for a feature like this, but as this is just a demonstration
		FeatureRegistrar.deregisterFeature(this)
	}

	// and that's all! a nice and simple way to do things powerfully!

	// the annotation used in this example,
	// it is encouraged to use a static inner annotation class with the name Attach
	// which will look like @KotlinWritingAFeature.Attach
	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	annotation class Attach
}
