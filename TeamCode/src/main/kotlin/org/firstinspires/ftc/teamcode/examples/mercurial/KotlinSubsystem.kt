package org.firstinspires.ftc.teamcode.examples.mercurial

import com.qualcomm.robotcore.hardware.DcMotorEx
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.mercurial.commands.Command
import dev.frozenmilk.mercurial.commands.LambdaCommand
import dev.frozenmilk.mercurial.commands.stateful.StatefulLambdaCommand
import dev.frozenmilk.mercurial.subsystems.Subsystem
import dev.frozenmilk.mercurial.subsystems.SubsystemObjectCell
import dev.frozenmilk.util.cell.RefCell

// this is a kotlin object, its a lot like the singleton pattern
// Subsystems are a lot like Features, they get preloaded and registered
// when the Robot controller first boots up
object KotlinSubsystem : Subsystem {
	// the annotation class we use to attach this subsystem
	annotation class Attach
	// Subsystems use the core Feature system of Dairy to be attached to OpModes
	// we need to set up the dependencies, which at its simplest looks like this
	override val dependencies = generateDependencySet()
			// this is the standard attach annotation that is recommended for features
			// if you are using other features, you should add them as
			// dependencies as well
			// you can also use the annotation to set up and manage
			// declarative settings for your subsystem, if desired
			.includesExactlyOneOf(Attach::class.java)

	// SubsystemObjectCells get eagerly reevaluated at the start of every OpMode, if this subsystem is attached
	// this means that we can always rely on motor to be correct and up-to-date for the current OpMode
	// this can also work with Calcified
	val motor by SubsystemObjectCell(this) {
		FeatureRegistrar.activeOpMode.hardwareMap.get(DcMotorEx::class.java, "")
	}

	// we get the full benefit of the Dairy core feature set,
	// so we can use any hooks to run code around the code we end up writing
	// this gives us a lot of freedom to set up a complex and powerful subsystem

	// init code might go in here
	override fun preUserInitHook(opMode: Wrapper) {}
	// or here
	override fun postUserInitHook(opMode: Wrapper) {}

	// and you might put periodic code in these
	override fun preUserInitLoopHook(opMode: Wrapper) {}
	override fun preUserLoopHook(opMode: Wrapper) {}
	// or these
	override fun postUserInitLoopHook(opMode: Wrapper) {}
	override fun postUserLoopHook(opMode: Wrapper) {}

	// and stopping code can go in here
	override fun preUserStopHook(opMode: Wrapper) {}
	// or here
	override fun postUserStopHook(opMode: Wrapper) {}

	// all depending on what you need!
	// remember, you only need to write implementations for the hooks you actually use
	// the rest don't need to be added to the class, nice and clean

	//
	// Commands
	//
	// commands are the same as older mercurial!
	// lambda commands are once again, powerful tools for developing simple units of operation
	fun simpleCommand(): LambdaCommand {
		return LambdaCommand()
				.addRequirements(this)
				.setInit { motor.power = 0.4 }
				.setEnd { interrupted ->
					if (!interrupted) motor.power = 0.0
				}
	}

	// lambda commands have a new powerful extension, designed to work well with the Cell patterns in Dairy util
	// RefCell<Double> is an immutable reference with interior immutability
	// we could also use a LazyCell, or an OpModeLazyCell, or SubsystemObjectCell, depending on our needs
	// we need to manage the state ourselves, if we want to reset it at the start of each run of this command,
	// or if its persistent across runs, we are in control
	// note that, each copy of state is unique to each individual instance of this command
	// if we wanted shared state across all instances, we could capture state from this class instead
	fun statefulCommand(): StatefulLambdaCommand<RefCell<Double>> {
		return StatefulLambdaCommand(RefCell(0.0))
				// note that stateful lambda commands have all the same methods that
				// the regular lambda command has
				// and variants that also take access to state where appropriate
				.addRequirements(this)
				.setInit { state -> motor.power = 0.4 + state.get() }
				// every time this command ends, we increase the power next time we run it
				// this isn't a terribly practical example
				// but this is useful for PID controllers and similar, without
				// requiring the creation of a whole command class just to hold some state
				.setEnd { interrupted, state ->
					if (!interrupted) motor.power = 0.0
					state.accept(state.get() + 0.1)
				}
	}
}