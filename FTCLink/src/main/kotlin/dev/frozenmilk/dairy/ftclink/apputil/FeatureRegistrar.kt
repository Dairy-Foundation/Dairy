package dev.frozenmilk.dairy.ftclink.apputil

import android.annotation.SuppressLint
import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import java.lang.ref.WeakReference

object FeatureRegistrar : OpModeManagerNotifier.Notifications {
	/**
	 * features that are registered to potentially become active
	 */
	private val registeredFeatures: LinkedHashSet<WeakReference<Feature>> = linkedSetOf()

	/**
	 * features that have been activated via [resolveDependencies]
	 */
	private val activeFeatures: LinkedHashSet<WeakReference<Feature>> = linkedSetOf()

	/**
	 * the feature flag annotations of the active OpMode
	 */
	private val activeFlags: LinkedHashMap<Class<out Annotation>, Annotation> = linkedMapOf()

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many features are registered
	 */
	fun registerFeature(feature: Feature) {
		val weakRef = WeakReference(feature)
		registeredFeatures.add(weakRef)
		val resolved = resolveDependencies(mutableSetOf(feature), activeFeatures.map { it.get() }.filterNotNullTo(mutableSetOf()), activeFlags)
		if (resolved.contains(feature)) activeFeatures.add(weakRef)
	}

	/**
	 * ensures that each feature is currently activated, if not, will through a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 */
	fun checkFeatures(vararg features: Feature) {
		val resolved = resolveDependencies(mutableSetOf(*features), activeFeatures.map { it.get() }.filterNotNullTo(mutableSetOf()), activeFlags)
		// throws all the exceptions it came across! (this will actually only throw the first one it finds, but none-the-less)
		resolved.forEach {
			it.value.forEach { exception -> throw exception }
		}
	}

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many listeners are registered
	 */
	fun deregisterListener(feature: Feature) {
		registeredFeatures.remove(
				registeredFeatures.first {
					it.get() == feature
				}
		)
		activeFeatures.remove(
				activeFeatures.first {
					it.get() == feature
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

		resolveDependencies(
				registeredFeatures.map { it.get() }.filterNotNullTo(mutableSetOf()), // makes a copy of the set || todo test
				setOf(), // todo this may not be empty
				activeFlags
		).forEach { // todo do something with the exceptions, they probably need to be stored
			activeFeatures.add(WeakReference(it.key))
		}

		// replace the OpMode with a wrapper that the user never sees, but provides our hooks
		// todo may cause issues, should be good though

		val activeOpMode = OpModeManagerImpl::class.java.getDeclaredField("activeOpMode")

		activeOpMode.isAccessible = true

		val wrapped = OpModeWrapper(opMode, this)
		activeOpMode.set(opModeManager, wrapped)
	}

	fun onOpModePreInit(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserInitHook(opMode) }
	}

	fun onOpModePostInit(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserInitHook(opMode) }
	}

	fun onOpModePreInitLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserInitLoopHook(opMode) }
	}

	fun onOpModePostInitLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserInitLoopHook(opMode) }
	}

	fun onOpModePreStart(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserStartHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// we expose our own hook, rather than this one
	}

	fun onOpModePostStart(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserStartHook(opMode) }
	}

	fun onOpModePreLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserLoopHook(opMode) }
	}

	fun onOpModePostLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserLoopHook(opMode) }
	}

	fun onOpModePreStop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserStopHook(opMode) }
	}

	fun onOpModePostStop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserStopHook(opMode) }
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// we expose our own hook, rather than this one

		// empty active listeners and active flags
		activeFeatures.clear()
		activeFlags.clear()
	}
}