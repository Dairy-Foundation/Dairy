package dev.frozenmilk.dairy.milkman.messages.outgoing

import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.milkman.messages.MessageType

class RobotState() : OutgoingMessage() {
	override val type: MessageType = MessageType.ROBOT_STATE
	val activeOpModeName = FeatureRegistrar.activeOpMode?.name ?: "INTERNAL ERR: OpMode name unknown"
	val opModeState = FeatureRegistrar.opModeState
	val flavour = FeatureRegistrar.activeOpMode?.opModeType?.name ?: "SYSTEM"
}