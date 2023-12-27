package dev.frozenmilk.dairy.core

import android.annotation.SuppressLint
import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import dev.frozenmilk.dairy.core.dependencyresolution.DependencyResolutionFailureException
import dev.frozenmilk.dairy.core.dependencyresolution.FeatureDependencyResolutionFailureException
import dev.frozenmilk.dairy.core.dependencyresolution.plus
import dev.frozenmilk.dairy.core.dependencyresolution.resolveDependencies
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.cell.MirroredCell
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import java.lang.ref.WeakReference
import java.util.ArrayDeque
import java.util.Queue

object FeatureRegistrar : OpModeManagerNotifier.Notifications {
	/**
	 * features that are registered to potentially become active
	 */
	private var registeredFeatures: MutableSet<WeakReference<Feature>> = mutableSetOf()

	/**
	 * intermediary collection of features that need to be checked to be added to the active pool
	 */
	private val registrationQueue: Queue<WeakReference<Feature>> = ArrayDeque()

	/**
	 * features that have been activated via [resolveDependencies]
	 */
	private val activeFeatures: MutableSet<WeakReference<Feature>> = mutableSetOf()

	/**
	 * the feature flag annotations of the active OpMode
	 */
	private val activeFlags: MutableSet<Annotation> = mutableSetOf()

	/**
	 * if there is currently an [OpMode] active on the robot, should be true almost all of the time
	 */
	@JvmStatic
	var opModeActive: Boolean = false
		private set

	/**
	 * the mirror cell used to manage this
	 */
	private val activeOpModeMirroredCell by LazyCell {
		MirroredCell<OpMode>(opModeManager, "activeOpMode")
	}

	/**
	 * the currently active OpMode, contents may be undefined if [opModeActive] does not return true
	 */
	@JvmStatic
	var activeOpMode: OpModeWrapper
		get() = activeOpModeMirroredCell.get() as OpModeWrapper
		private set(value) = activeOpModeMirroredCell.accept(value)

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many features are registered
	 */
	@JvmStatic
	fun registerFeature(feature: Feature) {
		val weakRef = WeakReference(feature)
		registeredFeatures.add(weakRef)
		if (!opModeActive) return
		registrationQueue.add(weakRef)
	}

	private fun resolveRegistrationQueue() {
		if (registrationQueue.isEmpty()) return
		resolveDependencies(
				registrationQueue.mapNotNull { it.get() }, // makes a copy of the set
				activeFeatures.mapNotNull { it.get() },
				activeFlags
		).second.forEach {
			activeFeatures.add(WeakReference(it))
		}
		registrationQueue.clear()
	}

	/**
	 * ensures that each feature is currently activated, if not, will through a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 */
	@JvmStatic
	fun checkFeatures(opMode: OpMode, vararg features: Feature) {
		val resolved = resolveDependencies(features.toList(), activeFeatures.mapNotNull { it.get() }, opMode.javaClass.annotations.toList()).first
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
	@JvmStatic
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

	private fun cleanFeatures() {
		registeredFeatures = registeredFeatures.filter { it.get() != null }.toMutableSet()
	}

	override fun onOpModePreInit(opMode: OpMode) {
		cleanFeatures()

		// locate feature flags, and then populate active listeners
		activeFlags.addAll(opMode.javaClass.annotations)

		registrationQueue.addAll(registeredFeatures)
		resolveRegistrationQueue()

		// replace the OpMode with a wrapper that the user never sees, but provides our hooks
		activeOpMode = OpModeWrapper(opMode)
		opModeActive = true

		// resolves the queue of anything that was registered later
		resolveRegistrationQueue()
	}

	@JvmStatic
	fun onOpModePreInit(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.preUserInitHook(opMode) }
	}

	@JvmStatic
	fun onOpModePostInit(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.postUserInitHook(opMode) }
	}

	@JvmStatic
	fun onOpModePreInitLoop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.preUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun onOpModePostInitLoop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.postUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun onOpModePreStart(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.preUserStartHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// we expose our own hook, rather than this one
	}

	@JvmStatic
	fun onOpModePostStart(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.postUserStartHook(opMode) }
	}

	@JvmStatic
	fun onOpModePreLoop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.preUserLoopHook(opMode) }
	}

	@JvmStatic
	fun onOpModePostLoop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.postUserLoopHook(opMode) }
	}

	@JvmStatic
	fun onOpModePreStop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.preUserStopHook(opMode) }
	}

	@JvmStatic
	fun onOpModePostStop(opMode: OpModeWrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.get()?.postUserStopHook(opMode) }
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// we expose our own hook, rather than this one
		resolveRegistrationQueue()
		// empty active listeners and active flags
		activeFeatures.clear()
		activeFlags.clear()
		opModeActive = false
	}
}