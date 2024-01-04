package dev.frozenmilk.dairy.milkman.messages.outgoing

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType

data class ReturnOpModes(val metadata: List<List<MilkMan.OpModeMetaData>>) : OutgoingMessage() {
	override val type: MessageType = MessageType.RETURN_OP_MODES
}