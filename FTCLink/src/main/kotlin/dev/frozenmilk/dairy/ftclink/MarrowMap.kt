package dev.frozenmilk.dairy.ftclink

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.configuration.LynxConstants

// todo remove this I think, and switch to just the modules instantiating themselves, and being accessible through something like this
class MarrowMap(hardwareMap: HardwareMap) : OpModeManagerNotifier.Notifications {
	val modules: Array<CalcifiedModule> = hardwareMap.getAll(LynxModule::class.java).map {
		CalcifiedModule(it)
	}.toTypedArray()

	val controlHub = modules.filter { it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber) }[0]
	val expansionHub = modules.filter { !(it.lynxModule.isParent && LynxConstants.isEmbeddedSerialNumber(it.lynxModule.serialNumber)) }[0]
	override fun onOpModePreInit(opMode: OpMode?) {
		TODO("Not yet implemented")
	}

	override fun onOpModePreStart(opMode: OpMode?) {
		TODO("Not yet implemented")
	}

	override fun onOpModePostStop(opMode: OpMode?) {
		TODO("Not yet implemented")
	}
}