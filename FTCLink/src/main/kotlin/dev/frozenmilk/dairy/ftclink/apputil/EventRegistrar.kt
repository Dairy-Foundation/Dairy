package dev.frozenmilk.dairy.ftclink.apputil

import android.annotation.SuppressLint
import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import java.lang.ref.WeakReference

object EventRegistrar : OpModeManagerNotifier.Notifications {
	private val listeners: MutableSet<WeakReference<Listener>> = mutableSetOf()

	fun registerListener(listener: Listener) {
		listeners.add(WeakReference(listener))
	}

	fun deregisterListener(listener: Listener) {
		listeners.remove(
				listeners.first {
					it.get() == listener
				}
		)
	}

	@SuppressLint("StaticFieldLeak")
	private lateinit var opModeManager: OpModeManagerImpl

	@OnCreateEventLoop
	@JvmStatic
			/**
			 * registers this instance against the event loop, use this as the basis generator for all calcified operations for the moment
			 */
	fun registerSelf(context: Context, ftcEventLoop: FtcEventLoop) {
		ftcEventLoop.opModeManager.registerListener(this)
		this.opModeManager = ftcEventLoop.opModeManager
	}

	override fun onOpModePreInit(opMode: OpMode) {
		// todo may cause issues
		OpModeManagerImpl::class.java.getField("activeOpMode").set(opModeManager, OpModeWrapper(opMode, this))
		listeners.forEach { it.get()?.preUserInitHook(opMode as OpModeWrapper) }
	}

	fun onOpModePostInit(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.postUserInitHook(opMode) }
	}

	fun onOpModePreInitLoop(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.preUserInitLoopHook(opMode) }
	}

	fun onOpModePostInitLoop(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.postUserInitLoopHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		listeners.forEach { it.get()?.preUserStartHook(opMode as OpModeWrapper) }
	}

	fun onOpModePostStart(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.postUserStartHook(opMode) }
	}

	fun onOpModePreLoop(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.preUserLoopHook(opMode) }
	}

	fun onOpModePostLoop(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.postUserLoopHook(opMode) }
	}

	fun onOpModePreStop(opMode: OpModeWrapper) {
		listeners.forEach { it.get()?.preUserStopHook(opMode) }
	}

	override fun onOpModePostStop(opMode: OpMode) {
		listeners.forEach { it.get()?.postUserStopHook(opMode as OpModeWrapper) }
	}
}