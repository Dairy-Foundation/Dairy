package org.firstinspires.ftc.teamcode.examples.templating

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.mercurial.Mercurial
import dev.frozenmilk.mercurial.bindings.BoundBooleanSupplier
import java.lang.annotation.Inherited

// we can use a simple feature to add additional configuration logic to the system,
// and also ensure that it runs after other features
// e.g. we could lazily perform some gamepad configuration, if the user doesn't do anything themselves
object KotlinConfigurationFeature : Feature {
	// we need Mercurial to be attached before this
	// and we need our own @Attach annotation
	override val dependency = SingleAnnotation(Attach::class.java) and SingleFeature(Mercurial)

	// Our configuration code can go here
	private var gm1aBefore: BoundBooleanSupplier? = null
	private var gm2bBefore: BoundBooleanSupplier? = null
	override fun preUserInitHook(opMode: Wrapper) {
		// we'll store their starting states
		gm1aBefore = Mercurial.gamepad1.a
		gm2bBefore = Mercurial.gamepad2.b
	}

	// and here:
	override fun postUserInitHook(opMode: Wrapper) {
		// unreachable guard
		if (gm1aBefore == null || gm2bBefore == null) return
		// we'll use a try, finally block to clean up our code at the end of this
		try {
			// if they have been modified by other user code, then we don't apply our own configuration
			if (gm1aBefore != Mercurial.gamepad1.a || gm2bBefore != Mercurial.gamepad2.b) return
			// map gamepad1.a and gamepad2.b to be shared
			Mercurial.gamepad1.a = Mercurial.gamepad1.a or Mercurial.gamepad2.b
			Mercurial.gamepad2.b = Mercurial.gamepad1.a
		}
		finally {
			// this cleanup always gets run, despite our early return
			gm1aBefore = null
			gm2bBefore = null
			// we don't want to hold onto data that could be cleaned up by the gc
		}
	}

	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Inherited
	annotation class Attach
}