package dev.frozenmilk.dairy.calcified

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.calcified.gamepad.CalcifiedGamepad
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.cell.LazyCell

/**
 * enabled by having either @[DairyCore] or @[Calcify]
 */
object Calcified : Feature {
	/**
	 * @see Calcify.crossPollinate
	 */
	var crossPollinate = true
		private set

	/**
	 * @see Calcify.automatedCacheHandling
	 */
	var automatedCacheHandling = true
		private set

	/**
	 * enabled by having either @[DairyCore] or @[Calcify]
	 */
	override val dependencies = DependencySet(this)
			.includesExactlyOneOf(DairyCore::class.java, Calcify::class.java).bindOutputTo {
				when (it) {
					is Calcify -> {
						crossPollinate = it.crossPollinate
						automatedCacheHandling = it.automatedCacheHandling
					}

					else -> {
						crossPollinate = true
						automatedCacheHandling = true
					}
				}
			}

	init {
		FeatureRegistrar.registerFeature(this)
	}

	/**
	 * all calcified modules found this OpMode
	 */
	var modules: Array<CalcifiedModule> = emptyArray()
		private set

	private var boringGamepad1: Gamepad by LateInitCell()
	private var boringGamepad2: Gamepad by LateInitCell()

	private val gamepad1Cell = LazyCell { CalcifiedGamepad(boringGamepad1) }
	val gamepad1: CalcifiedGamepad by gamepad1Cell
	private val gamepad2Cell = LazyCell { CalcifiedGamepad(boringGamepad2) }
	val gamepad2: CalcifiedGamepad by gamepad2Cell

	private val controlHubCell = LazyCell {
		if (!FeatureRegistrar.opModeActive) throw IllegalStateException("OpMode not inited, cannot yet access the control hub")
		modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }.getOrNull(0) ?:throw IllegalStateException(("The control hub was not found, this may be an electronics issue"))
	}

	/**
	 * the first hub in [modules] that satisfies the conditions to be considered a control hub
	 */
	val controlHub: CalcifiedModule by controlHubCell

	private val expansionHubCell = LazyCell {
		if (!FeatureRegistrar.opModeActive) throw IllegalStateException("OpMode not inited, cannot yet access the expansion hub")
		modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }.getOrNull(0) ?: throw IllegalStateException(("The expansion hub was not found, this may be an electronics issue"))
	}

	/**
	 * the first hub in [modules] that satisfies the conditions to be considered an expansion hub
	 */
	val expansionHub: CalcifiedModule by expansionHubCell

	/**
	 * internal refresh caches, only refreshes if the automated process is enabled
	 */
	private fun refreshCaches() {
		if (automatedCacheHandling) modules.forEach { it.refreshBulkCache() }
	}

	/**
	 * should be run in stop if you want to clear the status of the hardware objects for the next user, otherwise modules and hardware will be cleared according to [crossPollinate]
	 */
	fun clearModules() {
		modules = emptyArray()
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {
		// if cross pollination is enabled, and the OpMode type is Teleop, then we want to keep our pre-existing modules and hubs
		// however, if we have no modules (like after a teleop or at the very start) then we want to find new modules too
		// if cross pollination is disabled, we only want to find new stuff if the modules are empty
		if(modules.isEmpty() || (crossPollinate && opMode.opModeType != OpModeWrapper.OpModeType.TELEOP)) {
			modules = opMode.hardwareMap.getAll(LynxModule::class.java).map {
				CalcifiedModule(it)
			}.toTypedArray()

			controlHubCell.invalidate()
			expansionHubCell.invalidate()
		}

		boringGamepad1 = opMode.gamepad1
		boringGamepad2 = opMode.gamepad2

		gamepad1Cell.invalidate()
		gamepad2Cell.invalidate()

		refreshCaches()
	}

	override fun postUserInitHook(opMode: OpModeWrapper) {
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
		refreshCaches()
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
		refreshCaches()
	}


	override fun postUserStartHook(opMode: OpModeWrapper) {
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
		refreshCaches()
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
		refreshCaches()
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {
		if (opMode.opModeType == OpModeWrapper.OpModeType.TELEOP) {
			clearModules()
		}
	}
}
