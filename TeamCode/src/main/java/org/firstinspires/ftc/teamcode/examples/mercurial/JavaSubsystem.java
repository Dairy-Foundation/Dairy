package org.firstinspires.ftc.teamcode.examples.mercurial;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.Set;

import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.mercurial.commands.LambdaCommand;
import dev.frozenmilk.mercurial.subsystems.Subsystem;
import dev.frozenmilk.mercurial.subsystems.SubsystemObjectCell;

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
	
	// the annotation class we use to attach this subsystem
	public @interface Attach{}
	// Subsystems use the core Feature system of Dairy to be attached to OpModes
	// we need to set up the dependencies, which at its simplest looks like this
	@NonNull
	@Override
	public Set<Dependency<?, ?>> getDependencies() {
		// this is the standard attach annotation that is recommended for features
		// if you are using other features, you should add them as
		// dependencies as well
		// you can also use the annotation to set up and manage
		// declarative settings for your subsystem, if desired
		return generateDependencySet()
				.includesExactlyOneOf(Attach.class);
	}
	
	// SubsystemObjectCells get eagerly reevaluated at the start of every OpMode, if this subsystem is attached
	// this means that we can always rely on motor to be correct and up-to-date for the current OpMode
	// this can also work with Calcified
	private final SubsystemObjectCell<DcMotorEx> motor = new SubsystemObjectCell<>(this, () -> FeatureRegistrar.getActiveOpMode().hardwareMap.get(DcMotorEx.class, ""));
	public DcMotorEx getMotor() {
		return motor.get();
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

	// all depending on what you need!
	// remember, you only need to write implementations for the hooks you actually use
	// the rest don't need to be added to the class, nice and clean

	//
	// Commands
	//
	// commands are the same as older mercurial!
	// lambda commands are once again, powerful tools for developing simple units of operation
	public static LambdaCommand simpleCommand() {
		return new LambdaCommand()
				.addRequirements(INSTANCE)
				.setInit(() -> INSTANCE.getMotor().setPower(0.4))
				.setEnd(interrupted -> {
					if (!interrupted) INSTANCE.getMotor().setPower(0.0);
				});
	}
}