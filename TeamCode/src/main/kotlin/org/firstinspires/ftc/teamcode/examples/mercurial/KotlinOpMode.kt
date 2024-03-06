package org.firstinspires.ftc.teamcode.examples.mercurial

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier
import dev.frozenmilk.dairy.pasteurized.SDKGamepad
import dev.frozenmilk.mercurial.Mercurial
import dev.frozenmilk.mercurial.bindings.BoundBooleanSupplier
import dev.frozenmilk.mercurial.bindings.BoundDoubleSupplier
import dev.frozenmilk.mercurial.bindings.BoundGamepad
import dev.frozenmilk.mercurial.commands.LambdaCommand
import dev.frozenmilk.mercurial.commands.groups.AdvancingGroup
import dev.frozenmilk.mercurial.commands.groups.ParallelGroup
import dev.frozenmilk.mercurial.commands.groups.RaceGroup
import dev.frozenmilk.mercurial.commands.groups.SequentialGroup
import dev.frozenmilk.mercurial.commands.util.ConditionalCommand
import dev.frozenmilk.mercurial.commands.util.SelectionCommand
import dev.frozenmilk.mercurial.commands.util.WaitCommand

// the command scheduler
@Mercurial.Attach
// the subsystem
@JavaSubsystem.Attach
class KotlinOpMode : OpMode() {
	override fun init() {
		// Mercurial's gamepad system is works on top of the Pasteurized one
		// so if we want to do any big gamepad setup, its best to do so using the Pasteurized system now,
		// then, when we get the Mercurial gamepads, they will build off the Pasteurized gamepads we set up
		// or, you can build a BoundGamepad like so:
		val boundGamepad = BoundGamepad(SDKGamepad(gamepad1))

		// we can set up commands to be run
		Mercurial.gamepad1.a
				// runs on the rising edge of button a
				.onTrue(JavaSubsystem.simpleCommand())
				// runs on the falling edge of button a
				.onFalse(JavaSubsystem.simpleCommand())
				// runs on the rising edge of button a, until the falling edge, then ends naturally
				.whileTrue(JavaSubsystem.simpleCommand())
				// runs on the falling edge of button a, until the rising edge, then ends naturally
				.whileFalse(JavaSubsystem.simpleCommand())
				// runs on the rising edge of button a, reschedules if it ends early, until the falling edge, then ends naturally
				.untilFalse(JavaSubsystem.simpleCommand())
				// runs on the falling edge of button a, reschedules if it ends early, until the rising edge, then ends naturally
				.untilTrue(JavaSubsystem.simpleCommand())
				// runs on the rising edge of button a, cancels on the next rising edge
				.toggleTrue(JavaSubsystem.simpleCommand())
				// runs on the falling edge of button a, cancels on the next falling edge
				.toggleFalse(JavaSubsystem.simpleCommand())
				// cancels on the rising edge of a
				.cancelOnTrue(JavaSubsystem.simpleCommand())
				// cancels on the falling edge of a
				.cancelOnFalse(JavaSubsystem.simpleCommand())

		// we can use all the EnhancedSupplier features for this, by wrapping them in their Bound equivalent
		// or by building them ourselves
		BoundBooleanSupplier(EnhancedBooleanSupplier { true })
				.onTrue(LambdaCommand())
		BoundBooleanSupplier { true }
				.onTrue(LambdaCommand())

		Mercurial.gamepad1.leftStickY.conditionalBindPosition()
				.lessThan(0.0)
				.bind()
				.onTrue(LambdaCommand())

		BoundDoubleSupplier { 0.0 }
				.conditionalBindPosition()
				.greaterThan(100.0)
				.bind()
				.onTrue(
						// we can also build command groups
						ParallelGroup()
								.addCommands(
										LambdaCommand(),
										JavaSubsystem.simpleCommand()
								)
				)
				.onFalse(
						SequentialGroup()
								.addCommands(
										ConditionalCommand(
												Mercurial.gamepad1.circle::state,
												LambdaCommand(),
												LambdaCommand()
										),
										WaitCommand(10.0)
								)
				)

		// other utilities and command groups
		// advances sequentially whenever you schedule it
		AdvancingGroup()
				.addCommands(
						LambdaCommand(),
						LambdaCommand()
				)

		// map-based selection system, that can be used to set up complex state machines
		// it is advised to use enums as the selection method, unless all indexing operations are
		// performed programmatically
		val selectionCommand = SelectionCommand("start")
		selectionCommand.addCommands(mapOf(
				"start" to LambdaCommand().addEnd{ selectionCommand.schedule("middle") },
				"middle" to LambdaCommand().addEnd{ if(it) selectionCommand.schedule("end") else selectionCommand.schedule("start") },
				"end" to LambdaCommand()
		))

		// you can control it externally in many ways
		Mercurial.gamepad2.b.onTrue(selectionCommand) // these two are equivalent
		Mercurial.gamepad2.b.onTrue(selectionCommand.selectionCommand())

		Mercurial.gamepad2.b.onTrue(selectionCommand.selectionCommand("end"))

		// cancels all other commands early when the first one finishes
		RaceGroup()
				.addCommands(WaitCommand(1.0), JavaSubsystem.simpleCommand())

		// if the deadline is set, cancels all others when that ends instead of the first to finish
		val waitCommand = WaitCommand(1.0)
		RaceGroup()
				.setDeadline(waitCommand)
				.addCommands(waitCommand, JavaSubsystem.simpleCommand())

		// not much else to it!
	}

	override fun loop() {
		// no real point to doing anything here now!
	}
}