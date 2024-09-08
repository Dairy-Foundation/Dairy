package org.firstinspires.ftc.teamcode.examples.featuredev.jdoc;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.lynx.LynxModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;

public final class BulkReads implements Feature {
	// first, we need to set up the dependency
	// this makes a rule that says:
	// "for this feature to receive updates about an OpMode, it must have @BulkReads.Attach"
	private Dependency<?> dependency = new SingleAnnotation<>(Attach.class);
	// getters and setters for dependency
	@NonNull
	@Override
	public Dependency<?> getDependency() {
		return dependency;
	}
	
	@Override
	public void setDependency(@NonNull Dependency<?> dependency) {
		this.dependency = dependency;
	}
	
	// we'll make the constructor private
	private BulkReads() {}
	// our singleton instance
	public static final BulkReads INSTANCE = new BulkReads();
	
	private List<LynxModule> modules;
	
	@Override
	public void preUserInitHook(@NonNull Wrapper opMode) {
		// collect and store the modules
		modules = opMode.getOpMode().hardwareMap.getAll(LynxModule.class);
		// set them to manual
		modules.forEach(lynxModule -> lynxModule.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL));
	}
	
	// now, in each pre phase, we'll clear the bulk cache
	// we do this in pre, as most calculations and updates happen during post
	@Override
	public void preUserInitLoopHook(@NonNull Wrapper opMode) {
		modules.forEach(LynxModule::clearBulkCache);
	}
	
	@Override
	public void preUserStartHook(@NonNull Wrapper opMode) {
		modules.forEach(LynxModule::clearBulkCache);
	}
	
	@Override
	public void preUserLoopHook(@NonNull Wrapper opMode) {
		modules.forEach(LynxModule::clearBulkCache);
	}
	
	// cleanup is a guaranteed run post stop
	// here, we'll drop our references to the modules
	@Override
	public void cleanup(@NonNull Wrapper opMode) {
		modules = null;
	}
	
	// the @BulkReads.Attach annotation
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Attach {}
}
