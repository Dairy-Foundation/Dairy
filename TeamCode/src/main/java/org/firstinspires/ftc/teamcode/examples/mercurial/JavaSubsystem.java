package org.firstinspires.ftc.teamcode.examples.mercurial;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.mercurial.commands.LambdaCommand;
import dev.frozenmilk.mercurial.commands.stateful.StatefulLambdaCommand;
import dev.frozenmilk.mercurial.subsystems.Subsystem;
import dev.frozenmilk.mercurial.subsystems.SubsystemObjectCell;
import dev.frozenmilk.util.cell.RefCell;
import kotlin.annotation.MustBeDocumented;

// this is a kotlin object, its a lot like the singleton pattern
// Subsystems are a lot like Features, they get preloaded and registered
// when the Robot controller first boots up
public class JavaSubsystem implements Subsystem {
	// we are working with java, so we don't have the kotlin object class
	// so we will do the work ourselves
	// this instance line is super important
	private static final JavaSubsystem INSTANCE = new JavaSubsystem();
	public static JavaSubsystem getInstance() {
		return INSTANCE;
	}
	
	private JavaSubsystem() {
		setDefaultCommand(simpleCommand());
	}
	
	// the annotation class we use to attach this subsystem
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@MustBeDocumented
	@Inherited
	public @interface Attach{}
	// Subsystems use the core Feature system of Dairy to be attached to OpModes
	// we need to set up the dependencies, which at its simplest looks like this
	private Dependency<?> dependency =
			// the default dependency ensures that mercurial is attached
			Subsystem.DEFAULT_DEPENDENCY
					// this is the standard attach annotation that is recommended for features
					// if you are using other features, you should add them as
					// dependencies as well
					// you can also use the annotation to set up and manage
					// declarative settings for your subsystem, if desired
					.and(new SingleAnnotation<>(Attach.class));
	
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
	
	// SubsystemObjectCells get eagerly reevaluated at the start of every OpMode, if this subsystem is attached
	// this means that we can always rely on motor to be correct and up-to-date for the current OpMode
	// this can also work with Calcified
	private final SubsystemObjectCell<DcMotorEx> motor = new SubsystemObjectCell<>(this, () -> FeatureRegistrar.getActiveOpMode().hardwareMap.get(DcMotorEx.class, ""));
	public static DcMotorEx getMotor() {
		return INSTANCE.motor.get();
	}

	// we get the full benefit of the Dairy core feature set,
	// so we can use any hooks to run code around the code we end up writing
	// this gives us a lot of freedom to set up a complex and powerful subsystem

	// init code might go in here
	@Override
	public void preUserInitHook(@NonNull Wrapper opMode) {}
	// or here
	@Override
	public void postUserInitHook(@NonNull Wrapper opMode) {}
	
	// and you might put periodic code in these
	@Override
	public void preUserInitLoopHook(@NonNull Wrapper opMode) {}
	@Override
	public void preUserLoopHook(@NonNull Wrapper opMode) {}
	// or these
	@Override
	public void postUserInitLoopHook(@NonNull Wrapper opMode) {}
	@Override
	public void postUserLoopHook(@NonNull Wrapper opMode) {}

	// and stopping code can go in here
	@Override
	public void preUserStopHook(@NonNull Wrapper opMode) {}
	// or here
	@Override
	public void postUserStopHook(@NonNull Wrapper opMode) {}
	
	// see the feature dev notes on when to use cleanup vs postStop
	@Override
	public void cleanup(@NonNull Wrapper opMode) {}
	
	// all depending on what you need!
	// remember, you only need to write implementations for the hooks you actually use
	// the rest don't need to be added to the class, nice and clean

	//
	// Commands
	//
	// commands are the same as older mercurial!
	// lambda commands are once again, powerful tools for developing simple units of operation
	@NonNull
	public static LambdaCommand simpleCommand() {
		return new LambdaCommand()
				.addRequirements(INSTANCE)
				.setInit(() -> getMotor().setPower(0.4))
				.setEnd(interrupted -> {
					if (!interrupted) getMotor().setPower(0.0);
				});
	}
	
	
	// lambda commands have a new powerful extension, designed to work well with the Cell patterns in Dairy util
	// RefCell<Double> is an immutable reference with interior immutability
	// we could also use a LazyCell, or an OpModeLazyCell, or SubsystemObjectCell, depending on our needs
	// we need to manage the state ourselves, if we want to reset it at the start of each run of this command,
	// or if its persistent across runs, we are in control
	// note that, each copy of state is unique to each individual instance of this command
	// if we wanted shared state across all instances, we could capture state from this class instead
	@NonNull
	public static StatefulLambdaCommand<RefCell<Double>> statefulCommand() {
		return new StatefulLambdaCommand<>(new RefCell<>(0.0))
				// note that stateful lambda commands have all the same methods that
				// the regular lambda command has
				// and variants that also take access to state where appropriate
				.addRequirements(INSTANCE)
				.setInit((state) -> getMotor().setPower(0.4 + state.get()))
				// every time this command ends, we increase the power next time we run it
				// this isn't a terribly practical example
				// but this is useful for PID controllers and similar, without
				// requiring the creation of a whole command class just to hold some state
				.setEnd((interrupted, state) -> {
					if (!interrupted) getMotor().setPower(0);
					state.accept(state.get() + 0.1);
				});
	}
}