package org.firstinspires.ftc.teamcode.examples.featuredev

import dev.frozenmilk.dairy.calcified.Calcified
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LateInitCell
import java.lang.annotation.Inherited

// Todo: in the full documentation it would be very nice to put out a quick guide to setting up and publishing a dairy core library on jitpack

// to write a feature using DairyCore is pretty simple
// the object / class just needs to implement the Feature interface
// most major features are a static object, but plenty of smaller things implement feature in order to
// make use of the automation and hooks that the system provides
// for instance, OpModeLazyCells are features, that use the system to eagerly evaluate themselves in init
object KotlinWritingAFeature : Feature {
	// this uses a cell to extract an annotation, we'll come back to this later
	private val attachCell = LateInitCell<Attach>()

	// the dependencies are important, and are a powerful way to set
	// up the conditions that cause this feature to be attached
	// See KotlinDependencies for the full dependencies overview
	// Dairy features only technically have one dependency, its just that that dependency
	// can be compound, to check and resolve multiple conditions
	override var dependency: Dependency<*> =
			// The 'Annotation' series of dependencies allow us to declare that we
			// are dependant on some selection of @Annotations
			SingleAnnotation(Attach::class.java)
					// callbacks can be bound to the resolution of a dependency
					.onResolve {
						// if @Attach had configuration options,
						// we could extract them and store them from here
					}
					// we could also store the output in a variable or cell or similar
					.onResolve(attachCell) and // the infix operator 'and' allows us to construct a compound property
					// we can add that we also depend on calcified
					SingleFeature(Calcified)

	// we can then use delegation to get nice, direct access to the collected information
	private val attach by attachCell

	// there are lots of dependency options provided, additionally, its fairly easy to write your own.
	// so explore more, to figure out what best suits your project.

	// this block ensures that this feature is registered as soon as it comes into existence
	// it is important that it comes after the declaration of the dependencies,
	// otherwise the dependencies won't exist when this gets registered, which will cause a silent crash,
	// which is painful to debug
	// HOWEVER, in this case, we don't need this, as this is a Kotlin object,
	// so the static instance gets automatically registered by Sinister
	init {
		//FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: Wrapper) {
		// code that runs before the user's init,
		// in this case it will also be run after Calcified's version of this gets run,
		// so we can safely assume that Calcified has been set up, and use its features

		// for instance, we could add something to the Calcified Device Map
		Calcified.controlHub.deviceMap
	}

	override fun postUserInitHook(opMode: Wrapper) {
		// hooks are provided for before and after each bit of user code

		// Between the feature registrar and the OpModeWrapper passed to this hook,
		// some extra utilities are made easily accessible

		// the same as the OpModeWrapper being passed here, this probably isn't useful to you,
		// and it would be bad practice to use it when you have the OpModeWrapper
		FeatureRegistrar.activeOpModeWrapper // but it exists none-the-less
		FeatureRegistrar.opModeRunning // if an OpMode is currently active

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
	}

	// cleanup and post stop are similar but slightly different, cleanup is crash-safe
	override fun cleanup(opMode: Wrapper) {
		// some features (not this one) might want to automatically deregister themselves after the OpMode
		// while this isn't really necessary, as features are held weakly, and will disappear if the user doesn't hold onto them

		// this would be terrible practice for a feature like this, but as this is just a demonstration
		// FeatureRegistrar.deregisterFeature(this)

		// we should also reset some things for next run
		attachCell.invalidate()
	}

	// and that's all! a nice and simple way to do things powerfully!

	// the annotation used in this example,
	// it is encouraged to use a static inner annotation class with the name Attach
	// which will look like @KotlinWritingAFeature.Attach
	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Inherited
	annotation class Attach
}
