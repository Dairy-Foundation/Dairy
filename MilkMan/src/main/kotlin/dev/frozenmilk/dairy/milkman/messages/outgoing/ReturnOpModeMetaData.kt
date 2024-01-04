package dev.frozenmilk.dairy.milkman.messages.outgoing

import dev.frozenmilk.dairy.milkman.MilkMan
import dev.frozenmilk.dairy.milkman.messages.MessageType
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class ReturnOpModeMetaData() : OutgoingMessage() {
	override val type: MessageType = MessageType.RETURN_OP_MODE_META_DATA

	val metadata =
			MilkMan.registeredOpModes()
			.filter { it.flavor != OpModeMeta.Flavor.SYSTEM }
			.doubleGroupSort()


//	private fun List<OpModeMeta>.groupSort() : List<List<OpModeMetaData>> = this
//			.groupBy { it.flavor }
//			.toSortedMap()
//			.map { (_, v) -> v.groupBy{ it.group } }
//			.map { it.toSortedMap() }
//			.map { typed -> typed.flatMap { (_, v) -> v.sortedBy{ it.name } } }
//			.map { list -> list.map { OpModeMetaData(it) } }

	private fun List<OpModeMeta>.doubleGroupSort() = this
			.groupBy { it.flavor.name }
			.toSortedMap()
			.mapValues { flavour ->
				flavour.value
						.map { OpModeMetaData(it) }
						.groupBy { it.group }
						.toSortedMap()
						.mapValues { group ->
							group.value.sortedBy { it.name }
						}
						.flatMap { it.value }
			}

	data class OpModeMetaData(val name: String, val group: String) {
		constructor(meta: OpModeMeta) : this(meta.name, meta.group)
	}
}