package dev.frozenmilk.dairy.calcified.hardware.sensor

import com.qualcomm.hardware.lynx.commands.core.LynxSetDIODirectionCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetSingleDIOOutputCommand
import com.qualcomm.robotcore.hardware.DigitalChannel
import dev.frozenmilk.dairy.calcified.gamepad.EnhancedBooleanSupplier
import dev.frozenmilk.dairy.calcified.hardware.CalcifiedModule
import java.util.function.Consumer
import java.util.function.Supplier

class DigitalInput(private val module: CalcifiedModule, private val port: Byte) : Supplier<Boolean> {
	init {
		LynxSetDIODirectionCommand(module.lynxModule, port.toInt(), DigitalChannel.Mode.INPUT).send()
	}
	private var cachedState: Boolean? = null

	override fun get(): Boolean {
		if (cachedState == null) cachedState = module.bulkData.getDigitalInput(port.toInt())
		return cachedState!!
	}

	fun clearCache() {
		cachedState = null
	}

	/**
	 * an [EnhancedBooleanSupplier] version of this
	 */
	var enhanced = EnhancedBooleanSupplier(this)
}

class DigitalOutput(private val module: CalcifiedModule, private val port: Byte) : Consumer<Boolean> {
	init {
		LynxSetDIODirectionCommand(module.lynxModule, port.toInt(), DigitalChannel.Mode.OUTPUT).send()
	}
	override fun accept(p0: Boolean) {
		LynxSetSingleDIOOutputCommand(module.lynxModule, port.toInt(), p0).send()
	}
}