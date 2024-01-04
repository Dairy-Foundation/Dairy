package dev.frozenmilk.dairy.milkman

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.milkman.messages.Message
import dev.frozenmilk.dairy.milkman.messages.gson
import dev.frozenmilk.dairy.milkman.messages.outgoing.OutgoingMessage
import dev.frozenmilk.dairy.milkman.messages.outgoing.ReturnOpModeMetaData
import dev.frozenmilk.dairy.milkman.messages.outgoing.RobotState
import dev.frozenmilk.dairy.milkman.notifier.Notifier
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException

class MilkManWSD(port: Int = 8110) : NanoWSD(port), Feature {
	override fun openWebSocket(handshake: IHTTPSession): WebSocket = MilkManWebSocket(this, handshake)
	private val notifiers  = listOf(
			Notifier(this, { FeatureRegistrar.opModeState }, { RobotState() }),
			Notifier(this, { MilkMan.registeredOpModes() }, { ReturnOpModeMetaData() })
	)

	private val sockets = mutableSetOf<MilkManWebSocket>()

	fun sendToAllSockets(message: OutgoingMessage) {
		sockets.forEach { it.send(message) }
	}

	fun registerSocket(socket: MilkManWebSocket) {
		sockets.add(socket)
	}

	fun deregisterSocket(socket: MilkManWebSocket) {
		sockets.remove(socket)
	}

	override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
			.yields()

	init {
		FeatureRegistrar.registerFeature(this)
	}

	private fun pollNotifiers() {
		notifiers.forEach { it.poll() }
	}

	override fun preUserInitHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun postUserInitHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun preUserInitLoopHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun postUserInitLoopHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun preUserStartHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun postUserStartHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun preUserLoopHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun postUserLoopHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun preUserStopHook(opMode: OpModeWrapper) = pollNotifiers()

	override fun postUserStopHook(opMode: OpModeWrapper) = pollNotifiers()
}

class MilkManWebSocket(val milkManWSD: MilkManWSD, handshakeRequest: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(handshakeRequest) {
	fun send(message: OutgoingMessage) {
		RobotLog.vv("MilkMan", "sending message of type ${message.type}")
		val json = message.toJson()
		RobotLog.vv("MilkMan", "json contents: $json")
		send(json)
	}
	override fun onOpen() {
		milkManWSD.registerSocket(this)

		RobotLog.vv("MilkMan", "opening socket")

		send(RobotState())
		send(ReturnOpModeMetaData())
	}

	override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode, reason: String, initiatedByRemote: Boolean) {
		RobotLog.vv("MilkMan", "closing socket, $code, $reason, $initiatedByRemote")
		milkManWSD.deregisterSocket(this)
	}

	override fun onMessage(message: NanoWSD.WebSocketFrame) {
		val incoming = gson.fromJson(message.textPayload, Message::class.java)
		RobotLog.vv("MilkMan", "received message of type ${incoming.type}")
		incoming.process(this)
	}

	override fun onPong(pong: NanoWSD.WebSocketFrame) {
		RobotLog.vv("MilkMan", "received pong $pong")
	}

	override fun onException(exception: IOException) {
		RobotLog.dd("MilkMan", "internal exception: $exception")
	}
}

