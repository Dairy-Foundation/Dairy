package dev.frozenmilk.dairy.calcified

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.core.DairyCore
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.OpModeWrapper
import dev.frozenmilk.util.cell.LazyCell

/**
 * enabled by having either @[DairyCore] or @[DairyCore.Calcify]
 */
object Calcified : Feature {
	/**
	 * enabled by having either @[DairyCore] or @[DairyCore.Calcify]
	 */
	override val dependencies = DependencySet(this)
			.includesExactlyOneOf(DairyCore::class.java, DairyCore.Calcify::class.java)

	init {
		FeatureRegistrar.registerFeature(this)
	}

	var modules: Array<CalcifiedModule> = emptyArray()
		private set

	val controlHub: CalcifiedModule by LazyCell {
		if (!FeatureRegistrar.opmodeActive) throw IllegalStateException("OpMode not inited, cannot yet access the control hub")
		modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }.getOrNull(0) ?:throw IllegalStateException(("The control hub was not found, this may be an electronics issue"))
	}

	val expansionHub: CalcifiedModule by LazyCell {
		if (!FeatureRegistrar.opmodeActive) throw IllegalStateException("OpMode not inited, cannot yet access the expansion hub")
		modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }.getOrNull(0) ?: throw IllegalStateException(("The expansion hub was not found, this may be an electronics issue"))
	}

	override fun preUserInitHook(opMode: OpModeWrapper) {
		modules = opMode.hardwareMap.getAll(LynxModule::class.java).map {
			CalcifiedModule(it)
		}.toTypedArray()


		modules.forEach { it.refreshBulkCache() }
	}

	override fun postUserInitHook(opMode: OpModeWrapper) {
	}

	override fun preUserInitLoopHook(opMode: OpModeWrapper) {
		modules.forEach { it.refreshBulkCache() }
	}

	override fun postUserInitLoopHook(opMode: OpModeWrapper) {
	}

	override fun preUserStartHook(opMode: OpModeWrapper) {
		modules.forEach { it.refreshBulkCache() }
	}


	override fun postUserStartHook(opMode: OpModeWrapper) {
	}

	override fun preUserLoopHook(opMode: OpModeWrapper) {
		modules.forEach { it.refreshBulkCache() }
	}

	override fun postUserLoopHook(opMode: OpModeWrapper) {
	}

	override fun preUserStopHook(opMode: OpModeWrapper) {
		modules.forEach { it.refreshBulkCache() }
	}

	override fun postUserStopHook(opMode: OpModeWrapper) {
	}
}