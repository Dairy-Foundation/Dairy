package dev.frozenmilk.dairy.core

import android.annotation.SuppressLint
import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import java.lang.ref.WeakReference
import java.util.ArrayDeque
import java.util.Queue

object FeatureRegistrar : OpModeManagerNotifier.Notifications {
	/**
	 * features that are registered to potentially become active
	 */
	private val registeredFeatures: MutableSet<WeakReference<Feature>> = mutableSetOf()

	/**
	 * intermediary collection of features that need to be checked to be added to the active pool
	 */
	private val registrationQueue: Queue<WeakReference<Feature>> = ArrayDeque()

	/**
	 * features that have been activated via [resolveDependenciesMap]
	 */
	private val activeFeatures: MutableList<WeakReference<Feature>> = mutableListOf()

	/**
	 * the feature flag annotations of the active OpMode
	 */
	private val activeFlags: MutableSet<Annotation> = mutableSetOf()

	var opmodeActive: Boolean = false
		private set

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many features are registered
	 */
	fun registerFeature(feature: Feature) {
		val weakRef = WeakReference(feature)
		registeredFeatures.add(weakRef)
		if (!opmodeActive) return
		registrationQueue.add(weakRef)
	}

	private fun resolveRegistrationQueue() {
		if (registrationQueue.isEmpty()) return
		val resolved = resolveDependenciesOrderedList(
				registrationQueue.mapNotNull { it.get() }, // makes a copy of the set
				activeFeatures.mapNotNull { it.get() },
				activeFlags
		)
		resolved.filter { it.second.isEmpty() }.forEach {
			activeFeatures.add(WeakReference(it.first))
		}
		registrationQueue.clear()
	}

	/**
	 * ensures that each feature is currently activated, if not, will through a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 */
	fun checkFeatures(opMode: OpMode, vararg features: Feature) {
		val resolved = resolveDependenciesMap(features.toList(), activeFeatures.mapNotNull { it.get() }, opMode.javaClass.annotations.toList())
		// throws all the exceptions it came across in one giant message, if we find any
		if (!features.all { resolved[it].isNullOrEmpty() }) {
			throw DependencyResolutionFailureException(resolved.values.fold(Exception("")) { exception: Exception, featureDependencyResolutionFailureExceptions: Set<FeatureDependencyResolutionFailureException> ->
				exception + featureDependencyResolutionFailureExceptions.fold(Exception("")) { failureException: Exception, featureDependencyResolutionFailureException: FeatureDependencyResolutionFailureException ->
					failureException + featureDependencyResolutionFailureException
				}
			}.message!!)
		}
	}

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many listeners are registered
	 */
	fun deregisterFeature(feature: Feature) {
		activeFeatures.remove(
				activeFeatures.first {
					it.get() == feature
				}
		)
		registeredFeatures.remove(
				registeredFeatures.first {
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
	fun registerSelf(@Suppress("UNUSED_PARAMETER") context: Context, ftcEventLoop: FtcEventLoop) {
		opModeManager = ftcEventLoop.opModeManager
		opModeManager.registerListener(this)
	}

	override fun onOpModePreInit(opMode: OpMode) {
		// locate feature flags, and then populate active listeners
		activeFlags.addAll(opMode.javaClass.annotations)

		registrationQueue.addAll(registeredFeatures)
		resolveRegistrationQueue()

		// replace the OpMode with a wrapper that the user never sees, but provides our hooks
		val activeOpMode = dev.frozenmilk.util.cell.MirroredCell<OpMode>(opModeManager, "activeOpMode")

		val wrapped = OpModeWrapper(opMode, this)
		activeOpMode.accept(wrapped)
		opmodeActive = true

		// resolves the queue of anything that was registered later
		resolveRegistrationQueue()
	}

	fun onOpModePreInit(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserInitHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePostInit(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserInitHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePreInitLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserInitLoopHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePostInitLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserInitLoopHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePreStart(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserStartHook(opMode) }
		resolveRegistrationQueue()
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// we expose our own hook, rather than this one
	}

	fun onOpModePostStart(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserStartHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePreLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserLoopHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePostLoop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserLoopHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePreStop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.preUserStopHook(opMode) }
		resolveRegistrationQueue()
	}

	fun onOpModePostStop(opMode: OpModeWrapper) {
		activeFeatures.forEach { it.get()?.postUserStopHook(opMode) }
		resolveRegistrationQueue()
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// we expose our own hook, rather than this one
		resolveRegistrationQueue()
		// empty active listeners and active flags
		activeFeatures.clear()
		activeFlags.clear()
		opmodeActive = false
	}
}