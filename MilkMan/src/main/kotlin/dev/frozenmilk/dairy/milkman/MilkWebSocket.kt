package dev.frozenmilk.dairy.milkman

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException


class WebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(handshakeRequest) {
	companion object {
		val server = object : NanoWSD(8110) {
			override fun openWebSocket(handshake: IHTTPSession?): WebSocket {
				TODO("Not yet implemented")
			}

		}
	}
	override fun onOpen() {
	}

	override fun onClose(code: NanoWSD.WebSocketFrame.CloseCode?, reason: String?, initiatedByRemote: Boolean) {
	}

	override fun onMessage(message: NanoWSD.WebSocketFrame?) {
	}

	override fun onPong(pong: NanoWSD.WebSocketFrame?) {
	}

	override fun onException(exception: IOException?) {
	}
}