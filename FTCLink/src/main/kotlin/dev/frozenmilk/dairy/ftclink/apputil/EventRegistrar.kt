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
	/**
	 * listeners that are registered to potentially become active
	 */
	private val registeredListeners: MutableSet<WeakReference<Listener>> = mutableSetOf()

	/**
	 * listeners that have been activated via the appropriate feature flag for this OpMode
	 */
	private val activeListeners: MutableSet<WeakReference<Listener>> = mutableSetOf()

	/**
	 * the feature flag annotations of the active OpMode
	 */
	private val activeFlags: MutableMap<Class<out Annotation>, Annotation> = mutableMapOf()

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many listeners are registered
	 */
	fun registerListener(listener: Listener) {
		val weakRef = WeakReference(listener)
		registeredListeners.add(weakRef)
		if (listener.dependencyManager.enabled(activeFlags.keys)) activeListeners.add(weakRef)
	}

	/**
	 * ensures that each listener is currently activated, if not, will through a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 */
	fun checkFeatures(vararg listeners: Listener) {
		activeListeners
				.forEach {
					it.get()?.dependencyManager?.resolveOrError(activeFlags.keys) ?: false
				}
	}

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many listeners are registered
	 */
	fun deregisterListener(listener: Listener) {
		registeredListeners.remove(
				registeredListeners.first {
					it.get() == listener
				}
		)
		activeListeners.remove(
				activeListeners.first {
					it.get() == listener
				}
		)
	}

	@SuppressLint("StaticFieldLeak")
	private lateinit var opModeManager: OpModeManagerImpl

	@OnCreateEventLoop
	@JvmStatic
			/**
			 * registers this instance against the event loop, automatically called by the FtcEventLoop, should not be called by the user
			 */
	fun registerSelf(context: Context, ftcEventLoop: FtcEventLoop) {
		ftcEventLoop.opModeManager.registerListener(this)
		this.opModeManager = ftcEventLoop.opModeManager
	}

	override fun onOpModePreInit(opMode: OpMode) {
		// locate feature flags, and then populate active listeners
		opMode.javaClass.annotations.forEach {
			activeFlags[it::class.java] = it
		}
		registeredListeners
				.filter {
					it.get()?.dependencyManager?.enabled(activeFlags.keys) ?: false
				}
				.forEach { activeListeners.add(it) }

		// replace the OpMode with a wrapper that the user never sees, but provides our hooks
		// todo may cause issues
		val activeOpMode = OpModeManagerImpl::class.java.getDeclaredField("activeOpMode")
		activeOpMode.isAccessible = true
		activeOpMode.set(opModeManager, OpModeWrapper(opMode, this))

		activeListeners.forEach { it.get()?.preUserInitHook(opMode as OpModeWrapper) }
	}

	fun onOpModePostInit(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.postUserInitHook(opMode) }
	}

	fun onOpModePreInitLoop(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.preUserInitLoopHook(opMode) }
	}

	fun onOpModePostInitLoop(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.postUserInitLoopHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		activeListeners.forEach { it.get()?.preUserStartHook(opMode as OpModeWrapper) }
	}

	fun onOpModePostStart(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.postUserStartHook(opMode) }
	}

	fun onOpModePreLoop(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.preUserLoopHook(opMode) }
	}

	fun onOpModePostLoop(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.postUserLoopHook(opMode) }
	}

	fun onOpModePreStop(opMode: OpModeWrapper) {
		activeListeners.forEach { it.get()?.preUserStopHook(opMode) }
	}

	override fun onOpModePostStop(opMode: OpMode) {
		activeListeners.forEach { it.get()?.postUserStopHook(opMode as OpModeWrapper) }

		// empty active listeners and active flags
		activeListeners.clear()
		activeFlags.clear()
	}
}