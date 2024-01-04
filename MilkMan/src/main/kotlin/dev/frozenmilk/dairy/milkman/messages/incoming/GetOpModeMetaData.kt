package dev.frozenmilk.dairy.milkman.messages.incoming

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType
import dev.frozenmilk.dairy.milkman.messages.outgoing.ReturnOpModes

class OpModeMetaData : IncomingMessage() {
	override val type: MessageType = MessageType.OP_MODE_META_DATA
	override fun generateResponse() = ReturnOpModes(
			MilkMan.registeredOpModes()
					.filter { it.type != "SYSTEM" }
					.groupSort()
	)

	override fun internalProcess() {}

	private fun List<MilkMan.OpModeMetaData>.groupSort() : List<List<MilkMan.OpModeMetaData>> = this
			.groupBy { it.type }
			.toSortedMap()
			.map { (_, v) -> v.groupBy{ it.group } }
			.map { it.toSortedMap() }
			.map { typed -> typed.flatMap { (_, v) -> v.sortedBy{ it.name } } }
}