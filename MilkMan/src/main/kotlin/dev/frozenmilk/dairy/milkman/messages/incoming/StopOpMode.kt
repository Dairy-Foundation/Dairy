package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType

class StopOpMode : IncomingMessage(){
	override val type: MessageType = MessageType.STOP_OP_MODE
	override fun internalProcess() {
		MilkMan.stopOpMode()
	}
}