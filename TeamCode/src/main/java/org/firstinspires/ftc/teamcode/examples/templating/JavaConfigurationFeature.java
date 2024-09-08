package org.firstinspires.ftc.teamcode.examples.templating;

import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation;
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.mercurial.Mercurial;
import dev.frozenmilk.mercurial.bindings.BoundBooleanSupplier;

// we can use a simple feature to add additional configuration logic to the system,
// and also ensure that it runs after other features
// e.g. we could lazily perform some gamepad configuration, if the user doesn't do anything themselves
public class JavaConfigurationFeature implements Feature {
	// we need Mercurial to be attached before this
	// and we need our own @Attach annotation
	private Dependency<?> dependency = new SingleAnnotation<>(Attach.class).and(new SingleFeature<>(Mercurial.INSTANCE));
	@NonNull
	@Override
	public Dependency<?> getDependency() {
		return dependency;
	}
	
	@Override
	public void setDependency(@NonNull Dependency<?> dependency) {
		this.dependency = dependency;
	}
	
	// Our configuration code can go here
	private BoundBooleanSupplier gm1aBefore = null;
	private BoundBooleanSupplier gm2bBefore = null;
	@Override
	public void preUserInitHook(@NonNull Wrapper opMode) {
		// we'll store their starting states
		gm1aBefore = Mercurial.gamepad1().a();
		gm2bBefore = Mercurial.gamepad2().b();
	}
	
	@Override
	public void postUserInitHook(@NonNull Wrapper opMode) {
		// and here:
		// unreachable guard
		if (gm1aBefore == null || gm2bBefore == null) return;
		// we'll use a try, finally block to clean up our code at the end of this
		try {
			// if they have been modified by other user code, then we don't apply our own configuration
			if (gm1aBefore != Mercurial.gamepad1().a() || gm2bBefore != Mercurial.gamepad2().b()) return;
			// map gamepad1.a and gamepad2.b to be shared
			Mercurial.gamepad1().a(Mercurial.gamepad1().a().or(Mercurial.gamepad2().b()));
			Mercurial.gamepad2().b(Mercurial.gamepad1().a());
		}
		finally {
			// this cleanup always gets run, despite our early return
			gm1aBefore = null;
			gm2bBefore = null;
			// we don't want to hold onto data that could be cleaned up by the gc
		}
	}
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface Attach {}
}
