package dev.frozenmilk.dairy.milkman.notifier

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.milkman.MilkManWSD
import dev.frozenmilk.dairy.milkman.messages.outgoing.OutgoingMessage
import java.util.function.Supplier

class Notifier(val milkManWSD: MilkManWSD, var delta: Supplier<Any?>, var messageMaker: Supplier<OutgoingMessage>) {
	private var prev: Any? = null

	fun poll() {
		val new = delta.get()
		if (new != prev) {
			RobotLog.vv("MilkMan", "notifying about $new")
			milkManWSD.sendToAllSockets(messageMaker.get())
			prev = new
		}
	}
}