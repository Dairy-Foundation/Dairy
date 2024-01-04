package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType

class InitOpMode(val name: String) : IncomingMessage() {
	override val type: MessageType = MessageType.INIT_OP_MODE
	override fun internalProcess() = MilkMan.initOpMode(name)
}