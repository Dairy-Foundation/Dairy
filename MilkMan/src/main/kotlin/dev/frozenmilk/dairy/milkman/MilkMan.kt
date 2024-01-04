package dev.frozenmilk.dairy.milkman

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.util.RobotLog
import com.qualcomm.robotcore.util.WebHandlerManager
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.milkman.messages.incoming.GetOpModeMetaData
import dev.frozenmilk.dairy.milkman.messages.outgoing.ReturnOpModeMetaData
import dev.frozenmilk.dairy.milkman.notifier.Notifier
import dev.frozenmilk.util.cell.LateInitCell
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar
import dev.frozenmilk.dairy.milkman.messages.outgoing.RobotState
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes

object MilkMan {
	init {
		RobotLog.vv("MilkMan", "loading library...")
		System.loadLibrary("milkman")
		RobotLog.vv("MilkMan", "...loaded library")
	}

	private var opModeManager by LateInitCell<OpModeManagerImpl>()
	private var eventLoop by LateInitCell<FtcEventLoop>()

	/**
	 * registers this instance against the event loop, automatically called by the FtcEventLoop, should not be called by the user
	 */
	@OnCreateEventLoop
	@JvmStatic
	fun registerSelf(@Suppress("UNUSED_PARAMETER") context: Context, ftcEventLoop: FtcEventLoop) {
		eventLoop = ftcEventLoop
		opModeManager = ftcEventLoop.opModeManager as OpModeManagerImpl
	}

	private var loaded = false

	/**
	 * registers this instance against the web handler, automatically called by the FtcEventLoop, should not be called by the user
	 */
	@WebHandlerRegistrar
	@JvmStatic
	@Suppress("UNUSED_PARAMETER")
	fun registerAppRouter(context: Context, webHandlerManager: WebHandlerManager) {
		RobotLog.vv("MilkMan", "requesting router load")
		val isAlive = isAlive()
		RobotLog.vv("MilkMan", "isAlive: $isAlive")
		if(loaded) return
		RobotLog.vv("MilkMan", "initing app router...")
		initAppRouter()
		RobotLog.vv("MilkMan", "...inited app router")
		RobotLog.vv("MilkMan", "initing robot communication server...")
		// for some reason the websocket on the robot just chucks a hissy fit if this is low, and handles closing fine if this is high
		// anyway, this really isn't a concern if it's high, as htmx ws and tokio_tungstenite are much better technologies
		// and don't behave like children
		// anyway, this shouldn't complain
		MilkManWSD().start(Int.MAX_VALUE)

		RobotLog.vv("MilkMan", "...inited robot communication server")

		loaded = true
	}

//	@OnCreateMenu
//	@JvmStatic
//	fun populateMenu(@Suppress("UNUSED_PARAMETER") context: Context, menu: Menu) {
//		// todo
//		val enable = menu.add(Menu.NONE, Menu.NONE, 700, "Enable MilkMan")
//		val disable = menu.add(Menu.NONE, Menu.NONE, 700, "Disable MilkMan")
//
//		enable.setVisible()
//	}
//
	/**
	 * todo work on all of this
	 */
	external fun isAlive(): Boolean
	private external fun startRouter(port: Int)

	private var axumThreadCell by LateInitCell<Thread>()
	fun initAppRouter(port: Int = 8109) {
		axumThreadCell = Thread {
			startRouter(port)
		}

		axumThreadCell.start()
	}

	//
	// native utils
	// these are called by the native code in order to lessen the number of cross over points, and to transfer only the data we care about
	//

//	private class RegisteredOpModesUtil(val registeredOpModes: RegisteredOpModes) : RegisteredOpModes() {
//		override fun getOpModes(): MutableList<OpModeMeta> {
//			return if (opmodesAreRegistered) registeredOpModes.opModes
//			else mutableListOf()
//		}
//	}
//
	fun registeredOpModes(): List<OpModeMeta> {
//		RobotLog.vv("MilkMan", "requested registeredOpModes")
		val registeredOpModes = RegisteredOpModes.getInstance()

//		RobotLog.vv("MilkMan", "waiting for opmodes to be registered")
		registeredOpModes.waitOpModesRegistered()

//		RobotLog.vv("MilkMan", "opmodes registered, returning data")
		return registeredOpModes.opModes
	}
	internal fun stopOpMode() {
		FeatureRegistrar.activeOpMode?.requestOpModeStop()
	}
	internal fun startOpMode() {
		opModeManager.startActiveOpMode()
	}
	internal fun initOpMode(name: String) {
		opModeManager.initOpMode(name)
	}
//	@JvmStatic
//	fun activeOpMode(): OpModeMetaData? {
//		return registeredOpModes().firstOrNull { it.name == opModeManager.activeOpModeName }
//	}

//	fun opModeState(): RobotState {
//		return opModeManager.robotState
//	}

//	private fun initAppRouter(webHandlerManager: WebHandlerManager, assetManager: AssetManager) {
//		webHandlerManager.register("/milkman", makeWebHandler(assetManager, "milkman/index.html"))
//		webHandlerManager.register("/milkman/", makeWebHandler(assetManager, "milkman/index.html"))
//	}
//
//	private fun findAssets(webHandlerManager: WebHandlerManager, assetManager: AssetManager, filePath: String) {
//		val result = assetManager.list(filePath) ?: return
//
//		if (result.isEmpty()) {
//			webHandlerManager.register(
//					"/$filePath",
//					makeWebHandler(assetManager, filePath)
//			)
//		}
//		else {
//			result.forEach {
//				findAssets(webHandlerManager, assetManager, it)
//			}
//		}
//	}
//
//	private fun makeWebHandler(assetManager: AssetManager, filePath: String): WebHandler = WebHandler {
//		return@WebHandler if (it.method != NanoHTTPD.Method.GET) {
//			NanoHTTPD.newFixedLengthResponse (
//					NanoHTTPD.Response.Status.NOT_FOUND,
//					NanoHTTPD.MIME_PLAINTEXT,
//					"Expected a GET request?"
//			)
//		}
//		else {
//			NanoHTTPD.newChunkedResponse(
//					NanoHTTPD.Response.Status.OK,
//					MimeTypesUtil.determineMimeType(filePath),
//					assetManager.open(filePath)
//			)
//		}
//	}
}
