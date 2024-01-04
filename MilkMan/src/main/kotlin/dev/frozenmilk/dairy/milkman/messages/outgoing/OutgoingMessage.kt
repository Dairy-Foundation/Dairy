package dev.frozenmilk.dairy.milkman.messages.outgoing

import dev.frozenmilk.dairy.milkman.MilkManWebSocket
import dev.frozenmilk.dairy.milkman.messages.Message

abstract class OutgoingMessage: Message {
	final override fun process(milkManWebSocket: MilkManWebSocket) {}
}