package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType
import dev.frozenmilk.dairy.milkman.messages.outgoing.OutgoingMessage

class StartOpMode : IncomingMessage() {
	override val type: MessageType = MessageType.START_OP_MODE

	override fun internalProcess() {
		MilkMan.startOpMode()
	}
}