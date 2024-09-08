package org.firstinspires.ftc.teamcode.examples.featuredev;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.frozenmilk.dairy.calcified.Calcified;
import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation;
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.util.cell.LateInitCell;
import kotlin.annotation.MustBeDocumented;

// to write a feature using DairyCore is pretty simple
// the object / class just needs to implement the Feature interface
// most major features are a static object, but plenty of smaller things implement feature in order to
// make use of the automation and hooks that the system provides
// for instance, OpModeLazyCells are features, that use the system to eagerly evaluate themselves in init
public class JavaWritingAFeature implements Feature {
	// this uses a cell to extract an annotation, we'll come back to this later
	private final LateInitCell<Attach> attachCell = new LateInitCell<>();
	
	// the dependencies are important, and are a powerful way to set
	// up the conditions that cause this feature to be attached
	// See JavaDependencies for the full dependencies overview
	// Dairy features only technically have one dependency, its just that that dependency
	// can be compound, to check and resolve multiple conditions
	private Dependency<?> dependency =
			// The 'Annotation' series of dependencies allow us to declare that we
			// are dependant on some selection of @Annotations
			new SingleAnnotation<>(Attach.class)
					// callbacks can be bound to the resolution of a dependency
					.onResolve((attach) -> {
						// if @Attach had configuration options,
						// we could extract them and store them from here
					})
					// we could also store the output in a variable or cell or similar
					.onResolve(attachCell)
					.and( // the method 'and' allows us to construct a compound property
							// we can add that we also depend on calcified
							new SingleFeature<>(Calcified.INSTANCE)
					);
	
	// we need to have the getter, rather than the field,
	// but if we actually constructed the dependency every time we ran this, it would slow the program down
	@NonNull
	@Override
	public Dependency<?> getDependency() {
		return dependency;
	}
	
	@Override
	public void setDependency(@NonNull Dependency<?> dependency) {
		this.dependency = dependency;
	}
	
	// we can then use the java equivalent of delegation to get direct access to the collected information
	private Attach getAttach() {
		return attachCell.get();
	}
	
	// there are lots of dependency options provided, additionally, its fairly easy to write your own.
	// so explore more, to figure out what best suits your project.
	
	// this constructor ensures that this feature is registered as soon as it comes into existence
	// it is important that it comes after the declaration of the dependencies,
	// otherwise the dependencies won't exist when this gets registered, which will cause a silent crash,
	// which is painful to debug
	// HOWEVER, in this case, we don't need the registration line, as the static instance gets automatically registered
	private JavaWritingAFeature() {
		//FeatureRegistrar.registerFeature(this);
	}
	
	// these set up a singleton pattern for this feature
	// it might be good to make static methods that route through this instance
	public static final JavaWritingAFeature INSTANCE = new JavaWritingAFeature();

	@Override
	public void preUserInitHook(@NotNull Wrapper opMode) {
		// code that runs before the user's init,
		// in this case it will also be run after Calcified's version of this gets run,
		// so we can safely assume that Calcified has been set up, and use its features

		// for instance, we could add something to the Calcified Device Map
		Calcified.getControlHub().getDeviceMap();
	}

	@Override
	public void postUserInitHook(@NotNull Wrapper opMode) {
		// hooks are provided for before and after each bit of user code

		// Between the feature registrar and the OpModeWrapper passed to this hook,
		// some extra utilities are made easily accessible

		// the same as the OpModeWrapper being passed here, this probably isn't useful to you,
		// and it would be bad practice to use it when you have the OpModeWrapper
		FeatureRegistrar.getActiveOpModeWrapper(); // but it exists none-the-less
		FeatureRegistrar.isOpModeRunning(); // if an OpMode is currently active

		opMode.getOpModeType(); // teleop | autonomous | none

		// the OpModeWrapper also provides access to all the parts of an OpMode you might normally access
		Telemetry telemetry = opMode.getOpMode().telemetry; // the telemetry
		HardwareMap hardwareMap = opMode.getOpMode().hardwareMap; // the hardwareMap
	}

	// note that unused methods can be left unimplemented as they default to doing nothing, but for this demo all can be left in
	@Override
	public void preUserInitLoopHook(@NotNull Wrapper opMode) {
	}

	@Override
	public void postUserInitLoopHook(@NotNull Wrapper opMode) {
	}

	@Override
	public void preUserStartHook(@NotNull Wrapper opMode) {
	}

	@Override
	public void postUserStartHook(@NotNull Wrapper opMode) {
	}

	@Override
	public void preUserLoopHook(@NotNull Wrapper opMode) {
	}

	@Override
	public void postUserLoopHook(@NotNull Wrapper opMode) {
	}
	
	@Override
	public void preUserStopHook(@NotNull Wrapper opMode) {
	}
	
	@Override
	public void postUserStopHook(@NonNull Wrapper opMode) {
	}
	
	// cleanup and post stop are similar but slightly different, cleanup is crash-safe
	@Override
	public void cleanup(@NotNull Wrapper opMode) {
		// some features (not this one) might want to automatically deregister themselves after the OpMode
		// while this isn't really necessary, as features are held weakly, and will disappear if the user doesn't hold onto them

		// this would be terrible practice for a feature like this, but as this is just a demonstration
		// FeatureRegistrar.deregisterFeature(this);
		
		// we should also reset some things for next run
		attachCell.invalidate();
	}
	// and that's all! a nice and simple way to do things powerfully!
	
	// the annotation used in this example,
	// it is encouraged to use a static inner annotation class with the name Attach
	// which will look like @JavaWritingAFeature.Attach
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@MustBeDocumented
	@Inherited
	public @interface Attach {}
}

