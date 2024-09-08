package org.firstinspires.ftc.teamcode.examples.featuredev.jdoc;

import androidx.annotation.NonNull;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;

public class PID implements Feature {
	// first, we need to set up the dependency
	// Yielding just says "this isn't too important, always attach me, but run me after more important things"
	// Yielding is reusable!
	private Dependency<?> dependency = Yielding.INSTANCE;
	@NonNull
	@Override
	public Dependency<?> getDependency() {
		return dependency;
	}
	
	@Override
	public void setDependency(@NonNull Dependency<?> dependency) {
		this.dependency = dependency;
	}
	
	public PID(/* encoder, motor, coefficients... */) {
		// store them...
	}
	
	{
		// regardless of constructor used, call register when the class is instantiated
		register();
	}
	
	private void update() {
		// calculate next output using encoder, target and coefficients
		
		// don't update motor power if the controller isn't enabled
		if (!enabled) return;
		
		// set motor power to calculated output
	}
	
	// users should be able to change the target
	private int target = 0;
	
	public int getTarget() {
		return target;
	}
	
	public void setTarget(int target) {
		this.target = target;
	}
	
	// users should be able to enable / disable the controller
	private boolean enabled = true;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	// after init loop and loop we will update the controller
	@Override
	public void postUserInitLoopHook(@NonNull Wrapper opMode) {
		update();
	}
	
	@Override
	public void postUserLoopHook(@NonNull Wrapper opMode) {
		update();
	}
	
	// in cleanup we deregister, which prevents this from sticking around for another OpMode,
	// unless the user calls register again
	@Override
	public void cleanup(@NonNull Wrapper opMode) {
		deregister();
	}
}
