package org.firstinspires.ftc.teamcode.examples.mercurial;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier;
import dev.frozenmilk.dairy.pasteurized.SDKGamepad;
import dev.frozenmilk.mercurial.Mercurial;
import dev.frozenmilk.mercurial.bindings.BoundBooleanSupplier;
import dev.frozenmilk.mercurial.bindings.BoundDoubleSupplier;
import dev.frozenmilk.mercurial.bindings.BoundGamepad;
import dev.frozenmilk.mercurial.commands.Lambda;
import dev.frozenmilk.mercurial.commands.groups.Advancing;
import dev.frozenmilk.mercurial.commands.groups.Parallel;
import dev.frozenmilk.mercurial.commands.groups.Race;
import dev.frozenmilk.mercurial.commands.groups.Sequential;
import dev.frozenmilk.mercurial.commands.stateful.StatefulLambda;
import dev.frozenmilk.mercurial.commands.util.IfElse;
import dev.frozenmilk.mercurial.commands.util.StateMachine;
import dev.frozenmilk.mercurial.commands.util.Wait;
import dev.frozenmilk.util.cell.RefCell;

// the command scheduler
@Mercurial.Attach
// the subsystem
@JavaSubsystem.Attach
public class JavaOpMode extends OpMode {
	@Override
	public void init() {
		// Mercurial's gamepad system is works on top of the Pasteurized one
		// so if we want to do any big gamepad setup, its best to do so using the Pasteurized system now,
		// then, when we get the Mercurial gamepads, they will build off the Pasteurized gamepads we set up
		// or, you can build a BoundGamepad like so:
		BoundGamepad boundGamepad = new BoundGamepad(new SDKGamepad(gamepad1));

		// we can set up commands to be run
		Mercurial.gamepad1().a()
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
				.cancelOnFalse(JavaSubsystem.simpleCommand());

		// we can use all the EnhancedSupplier features for this, by wrapping them in their Bound equivalent
		// or by building them ourselves
		new BoundBooleanSupplier(new EnhancedBooleanSupplier(() -> true))
				.onTrue(new Lambda("demo"));
		new BoundBooleanSupplier(() -> true)
				.onTrue(new Lambda("demo"));

		Mercurial.gamepad1().leftStickY().conditionalBindState()
				.lessThan(0.0)
				.bind()
				.onTrue(new Lambda("demo"));

		new BoundDoubleSupplier(() -> 0.0)
				.conditionalBindState()
				.greaterThan(100.0)
				.bind()
				.onTrue(
						// we can also build command groups
						// Parallel runs commands at the same time
						new Parallel(
								new Lambda("demo"),
								JavaSubsystem.simpleCommand()
						)
				)
				.onFalse(
						// sequential runs commands in order
						new Sequential(
								// IfElse picks a command when initialised
								// using the first argument
								// then runs true or false accordingly
								new IfElse(
										() -> Mercurial.gamepad1().circle().state(),
										new Lambda("true"),
										new Lambda("false")
								),
								// wait runs for duration seconds
								new Wait(10.0)
						)
				);

		// other utilities and command groups
		// advances sequentially whenever you schedule it
		// also wraps around
		Advancing advancing = new Advancing(
				new Lambda("demo"),
				new Lambda("demo")
		);
		
		// this isn't super useful in another group
		// but allows you to easily set up some alternating
		// behaviour on a user input
		// so consider binding it to a button
		
		// if you want to advance it by hand, this will advance it
		// but it needs to be run separately
		// you can't advance more than once
		// so calling another advance() won't do anything
		// until the advancement has been processed
		advancing.advance();

		// map-based selection system, that can be used to set up complex state machines
		// it is advised to use enums as the selection method, unless all indexing operations are
		// performed programmatically
		StateMachine<States> selectionCommand = new StateMachine<>(States.START)
				// withState is a non-mutating method
				// it takes a state,
				// and a function that takes in a RefCell to the state along with an
				// escaped name that matches the name of the state
				.withState(States.START, (RefCell<States> state, String name) ->
						// we can use our own name, or use the name from the lambda to use the name of the state
						new Lambda("demo")
								// to change the state, we can enter a new state in the cell
								// if the new state isn't different, then nothing will happen
								// but if you set the state to something different, then back to the
								// previous state, then the current command will be cancelled and restarted
								.addEnd((interrupted) -> state.accept(States.MIDDLE))
				)
				.withState(States.MIDDLE, (state, name) ->
						new Lambda(name)
								.addEnd(interrupted -> {
									if (interrupted) {
										state.accept(States.END);
									}
									else {
										state.accept(States.START);
									}
								})
				)
				.withState(States.END, (state, name) -> new Lambda(name));
		
		// we can also set the state externally
		selectionCommand.setState(States.START);

		// a set up like this will do a 'double bind'
		// it will ensure that selectionCommand is running
		// and set its state regardless
		Mercurial.gamepad2().b().onTrue(selectionCommand);
		Mercurial.gamepad2().b().onTrue(
				new Lambda("set-end")
						.setEnd(interrupted -> selectionCommand.setState(States.END))
		);

		// cancels all other commands early when the first one finishes
		new Race(null, new Wait(1.0), JavaSubsystem.simpleCommand());

		// if the deadline is set, cancels all others when that ends instead of the first to finish
		new Race(new Wait(1.0), JavaSubsystem.simpleCommand());

		// there are also utility methods to build many of these command structures
		// e.g.:
		
		new Lambda("demo")
				// Sequential
				.then(new Lambda("sequence"));
		
		new Lambda("demo")
				// Parallel
				.with(new Lambda("parallel"));
		
		new Lambda("demo")
				// Race with no deadline
				.raceWith(new Lambda("race"));
		
		new Lambda("demo")
				// Race with demo as deadline
				.asDeadline(new Lambda("race"));
		
		new Lambda("demo")
				// race with a Wait(duration) as deadline
				.timeout(1.0);
		
		// you can convert any command into a Lambda like so
		// and then add additional phases to its execution
		Lambda lambdaFrom = Lambda.from(new Sequential());
		
		// you can convert any command into a StatefulLambda like so
		// and then add additional phases to its execution
		StatefulLambda<String> statefulLambdaFrom = StatefulLambda.from(new Sequential(), "state");
		
		// not much else to it!
	}
	
	enum States {
		START,
		MIDDLE,
		END;
	}

	@Override
	public void loop() {
		// no real point to doing anything here now!
	}
}