package dev.frozenmilk.dairy.ftclink.calcified

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import dev.frozenmilk.dairy.ftclink.apputil.EventRegistrar
import dev.frozenmilk.dairy.ftclink.apputil.Listener
import dev.frozenmilk.dairy.ftclink.apputil.OpModeWrapper
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop

object MarrowMap : Listener {
	init {
		EventRegistrar.registerListener(this)
	}

	lateinit var modules: Array<CalcifiedModule>
		private set

	lateinit var controlHub: CalcifiedModule
		private set
	lateinit var expansionHub: CalcifiedModule
		private set

	override fun preUserInitHook(opMode: OpModeWrapper) {
		modules = opMode.hardwareMap.getAll(LynxModule::class.java).map {
			CalcifiedModule(it)
		}.toTypedArray()

		controlHub = modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }[0]
		expansionHub = modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }[0]

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