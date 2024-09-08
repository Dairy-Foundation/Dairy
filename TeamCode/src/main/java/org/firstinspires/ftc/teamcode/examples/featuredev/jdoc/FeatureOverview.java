package org.firstinspires.ftc.teamcode.examples.featuredev.jdoc;

import androidx.annotation.NonNull;

import java.util.List;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;

public class FeatureOverview implements Feature {
	{
		// returns true if this is currently active
		// true means it will receive updates for the current OpMode
		boolean isActive = isActive();
	}
	
	// we won't look at the dependency system closely here
	private Dependency<?> dependency = (Wrapper opMode, List<? extends Feature> resolvedFeatures, boolean yielding) -> null;
	
	@NonNull
	@Override
	public Dependency<?> getDependency() {
		return dependency;
	}
	
	@Override
	public void setDependency(@NonNull Dependency<?> dependency) {
		this.dependency = dependency;
	}
	
	//
	// Hooks
	//
	
	// By default, all the hooks are empty, so you only need to override the ones you want to use
	
	@Override
	public void preUserInitHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void postUserInitHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void preUserInitLoopHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void postUserInitLoopHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void preUserStartHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void postUserStartHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void preUserLoopHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void postUserLoopHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void preUserStopHook(@NonNull Wrapper opMode) {}
	
	@Override
	public void postUserStopHook(@NonNull Wrapper opMode) {}
	
	// cleanup differs from postUserStopHook, it runs after the OpMode has completely stopped,
	// and is guaranteed to run, even if the OpMode stopped from a crash.
	@Override
	public void cleanup(@NonNull Wrapper opMode) {}
	
	{
		// finally, lets look at some Feature related FeatureRegistrar methods
		
		FeatureRegistrar.getActiveFeatures(); // list of currently active features
		FeatureRegistrar.getRegisteredFeatures(); // list of registered features
		
		FeatureRegistrar.isFeatureActive(this); // boolean, same as Feature.isActive()
		
		// don't register and deregister Features a lot, its expensive
		// try to keep this to only during construction / init, or only one or two at runtime
		// the more you do, the more expensive it is
		FeatureRegistrar.registerFeature(this); // same as Feature.register()
		FeatureRegistrar.deregisterFeature(this); // same as Feature.deregister()
	}
}
