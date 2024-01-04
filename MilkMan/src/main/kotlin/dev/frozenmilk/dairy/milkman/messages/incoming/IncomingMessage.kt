package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkManWebSocket
import dev.frozenmilk.dairy.milkman.messages.Message
import dev.frozenmilk.dairy.milkman.messages.outgoing.OutgoingMessage

abstract class IncomingMessage : Message {
	open fun generateResponse() : OutgoingMessage? = null
	open fun internalProcess() {}
	final override fun process(milkManWebSocket: MilkManWebSocket) {
		internalProcess()
		val response = generateResponse()
		response?.run {
			milkManWebSocket.send(response)
		}
	}
}