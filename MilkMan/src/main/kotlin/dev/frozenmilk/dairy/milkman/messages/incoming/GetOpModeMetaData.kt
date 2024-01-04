package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType
import dev.frozenmilk.dairy.milkman.messages.outgoing.ReturnOpModeMetaData
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class GetOpModeMetaData : IncomingMessage() {
	override val type: MessageType = MessageType.GET_OP_MODE_META_DATA
	override fun generateResponse() = ReturnOpModeMetaData()

	override fun internalProcess() {}

}

