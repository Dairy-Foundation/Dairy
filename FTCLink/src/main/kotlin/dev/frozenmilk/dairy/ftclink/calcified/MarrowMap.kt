package dev.frozenmilk.dairy.ftclink.calcified

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop

object MarrowMap : OpModeManagerNotifier.Notifications {
	@OnCreateEventLoop
	@JvmStatic
			/**
			 * registers this marrowmap instance against the event loop, use this as the basis generator for all calcified operations for the moment
			 */
	fun registerSelf(context: Context, ftcEventLoop: FtcEventLoop) {
		ftcEventLoop.opModeManager.registerListener(this)
	}

	lateinit var modules: Array<CalcifiedModule>
		private set

	lateinit var controlHub: CalcifiedModule
		private set
	lateinit var expansionHub: CalcifiedModule
		private set

	override fun onOpModePreInit(opMode: OpMode) {
		modules = opMode.hardwareMap.getAll(LynxModule::class.java).map {
			CalcifiedModule(it)
		}.toTypedArray()

		controlHub = modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }[0]
		expansionHub = modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }[0]
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// todo maybe do something here, nothing atm though
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// todo maybe do something here
	}
}