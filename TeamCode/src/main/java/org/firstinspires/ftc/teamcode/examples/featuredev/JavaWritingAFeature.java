package org.firstinspires.ftc.teamcode.examples.featuredev;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import dev.frozenmilk.dairy.calcified.Calcified;
import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency;
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.util.cell.Cell;
import dev.frozenmilk.util.cell.LateInitCell;

// Todo: in the full documentation it would be very nice to put out a quick guide to setting up and publishing a dairy core library on jitpack

// to write a feature using DairyCore is pretty simple
// the object / class just needs to implement the Feature interface
// most major features are a static object, but plenty of smaller things implement feature in order to
// make use of the automation and hooks that the system provides
// for instance, OpModeLazyCells are features, that use the system to eagerly evaluate themselves in init
public class JavaWritingAFeature implements Feature {
	// this uses a cell to extract a feature, we'll come back to this later
	private final Cell<Feature> calcfiedCell = new LateInitCell<>();

	// the dependencies are important, we use the DependencySet() class to assist with this
	// the dependencies allow the FeatureRegistrar to determine the order in which our dependencies are attached
	@NonNull
	@Override
	@SuppressWarnings("unchecked") // java complains about this, but it isn't an issue, so we suppress the warning
	public Set<Dependency<?, ?>> getDependencies() {
		return new DependencySet(this)
				// this says that we need the @JavaMyFeature annotation to activate this
			.includesExactlyOneOf(Attach.class)
				// this allows us to run a piece of code when this feature gets activated, and the @JavaMyFeature annotation will be passed to it
			.bindOutputTo((annotation) -> {
				if (annotation instanceof Attach) {
					System.out.println("my feature activated!");
				}
				else {
					// this can't happen, because we only asked to find @KotlinMyFeature
					System.out.println("something else got here!");
				}
			})
		
				// this says we need calcified to be attached for this to be attached
			.dependsOnOneOf(Calcified.class)
				// outputs can also be bound to cells, instead of pieces of code, so we can extract the value to use again later
			.bindOutputTo(calcfiedCell)
				// this lets the dependency resolver know that this feature isn't very important,
				// and will wait until nothing else can be attached before it is
				// usually this isn't put on major features like this,
				// but, for the sake of demonstration...
		    .yields();
		
		// there are lots of dependency options here, that are fairly well documented, and hard to show without a much bigger example,
		// so explore more, to figure out what best suits your project.
		// remember, no dependency set can be empty, this will prevent your Feature from being attached,
		// and will result in unhelpful errors being thrown by its resolution process
	}
	
	// this constructor ensures that this feature is registered as soon as it comes into existence
	private JavaWritingAFeature() {
		FeatureRegistrar.registerFeature(this);
	}
	
	// these set up a singleton pattern for this feature
	// it might be good to make static methods that route through this instance
	private final JavaWritingAFeature instance = new JavaWritingAFeature();
	public JavaWritingAFeature getInstance() {
		return instance;
	}

	// this allows us to quickly get calcified out of the extraction cell, for use later
	// note: this doesn't work in java, as calcified is static, but if it wasn't this would be required
	// so this is left in, but unused
	private Calcified getCalcified() {
		return (Calcified) calcfiedCell.get();
	}

	@Override
	public void preUserInitHook(@NotNull Wrapper opMode) {
		// code that runs before the user's init,
		// in this case it will also be run after Calcified's version of this gets run,
		// so we can safely assume that Calcified has been set up, and use its features

		// this feature will perform a cross-controller remap
//		Calcified.getGamepad1().setA(Calcified.getGamepad1().getA().or(Calcified.getGamepad2().getA()));
	}

	@Override
	public void postUserInitHook(@NotNull Wrapper opMode) {
		// hooks are provided for before and after each bit of user code

		// Between the feature registrar and the OpModeWrapper passed to this hook,
		// some extra utilities are made easily accessible

		// the same as the OpModeWrapper being passed here, this probably isn't useful to you,
		// and it would be bad practice to use it when you have the OpModeWrapper
		FeatureRegistrar.getActiveOpModeWrapper(); // but it exists none-the-less
		FeatureRegistrar.getOpModeActive(); // if an OpMode is currently active

		opMode.getOpModeType(); // teleop | autonomous | none

		// the OpModeWapper also provides access to all the parts of an OpMode you might normally access
		Telemetry telemetry = opMode.getOpMode().telemetry; // the telemetry
		HardwareMap hardwareMap = opMode.getOpMode().hardwareMap; // the hardwareMap
	}

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
	public void postUserStopHook(@NotNull Wrapper opMode) {
		// some features (not this one) might want to automatically deregister themselves after the OpMode
		// while this isn't really necessary, as features are held weakly, and will disappear if the user doesn't hold onto them

		// this would be terrible practice for a feature like this, but as this is just a demonstration
		FeatureRegistrar.deregisterFeature(this);
	}
	// and that's all! a nice and simple way to do things powerfully!
	
	// the annotation used in this example,
	// it is encouraged to use a static inner annotation class with the name Attach
	// which will look like @JavaWritingAFeature.Attach
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Attach {}
}

