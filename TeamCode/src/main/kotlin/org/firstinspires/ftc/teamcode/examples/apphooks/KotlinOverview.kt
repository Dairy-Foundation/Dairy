package org.firstinspires.ftc.teamcode.examples.apphooks

import android.content.Context
import android.view.Menu
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.util.WebHandlerManager
import dev.frozenmilk.sinister.apphooks.OnCreate
import dev.frozenmilk.sinister.apphooks.OnCreateEventLoop
import dev.frozenmilk.sinister.apphooks.OnCreateMenu
import dev.frozenmilk.sinister.apphooks.OnDestroy
import dev.frozenmilk.sinister.apphooks.OpModeRegistrar
import dev.frozenmilk.sinister.apphooks.WebHandlerRegistrar

@Suppress("unused")
class KotlinOverview private constructor() {
	// we could do whatever we want here!
	/**
	 * this is a private static inner class, to prevent AppHook code from leaking to the public api
	 *
	 * Sinister's scanning looks for instances to work with. In kotlin, this is an object class, in Java we need to re-create that by hand.
	 */
	private object AppHook : OnCreate, OnCreateEventLoop, OnCreateMenu, OnDestroy, OpModeRegistrar, WebHandlerRegistrar {
		override fun onCreate(context: Context) {
			// runs when the activity is created
		}

		override fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop) {
			// runs when the event loop is created

			// this is how to get the OpModeManager
			val opModeManager = ftcEventLoop.opModeManager
		}

		override fun onCreateMenu(context: Context, menu: Menu) {
			// allows you to edit the menu on the control hub, not super useful
		}

		override fun onDestroy(context: Context) {
			// when the app shuts down
		}

		override fun registerOpModes(opModeManager: AnnotatedOpModeManager) {
			// allows you to register more OpModes yourself, combine this with a Sinister Filter for custom OpMode annotations and registration.
		}

		override fun webHandlerRegistrar(context: Context, webHandlerManager: WebHandlerManager) {
			// allows you to work with the web server
		}
	}
}