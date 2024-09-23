package org.firstinspires.ftc.teamcode.examples.mercurial

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.mercurial.commands.Lambda
import dev.frozenmilk.mercurial.commands.rename

class KotlinCommandNames : OpMode() {
	override fun init() {
		val lambda = Lambda("lambda")
			.setInit {
				throw RuntimeException("error")
			}
		// when this command initialises, it will throw an error
		// Mercurial will wrap this in a helpful message, letting us know that
		// the error occurred in the initialise phase
		// and that the command was named "lambda"
		// larger command group structures will print out a more helpful error

		// this Parallel command will print out this message:
		lambda.with(Lambda("other"))
		// "
		// exception thrown in initialise:
		// caused by: lambda
		// cause is marked as 'ERR' in this command s-expr
		// (parallel (
		// 	ERR
		// 	other))
		// "

		// which clearly points out that we are in a parallel group,
		// that the error was thrown in initialise
		// and that the command was named 'lambda'
		// and the other command in the group was named 'other'
		// don't worry, the stack trace will also contain the cause exception

		// this continues to work with larger and larger structures of commands
		// and different commands have slightly different s-expr structures to represent themselves

		// when naming a command this 'rename' function is used
		val commandName = rename("MyCoolCommandName")
		// 'MyCoolCommandName' will come out as 'my-cool-command-name'
		// but, if its important, we can use a backslash at the start of the command name to prevent modification
		// so:
		val unmodified = rename("\\MyCoolCommandName")
		// this will come out as 'MyCoolCommandName'

		// its best to just follow the all lower case dash separated system for commands
		// so that you get what you expect
	}

	override fun loop() {
	}
}