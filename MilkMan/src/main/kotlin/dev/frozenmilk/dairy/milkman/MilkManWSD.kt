package dev.frozenmilk.dairy.milkman

import fi.iki.elonen.NanoWSD

internal class MilkManWebSocketManager(port: Int) : NanoWSD(port) {
	override fun openWebSocket(handshake: IHTTPSession): WebSocket = MilkManWebSocket(handshake)
}