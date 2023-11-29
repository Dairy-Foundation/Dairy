package dev.frozenmilk.dairy.ftclink.apputil

import com.qualcomm.robotcore.eventloop.opmode.OpMode

class OpModeWrapper(private val opMode: OpMode, private val eventRegistrar: EventRegistrar) : OpMode() {
	init {
		// may allow passthrough to these user accessed values
		opMode.gamepad1 = this.gamepad1
		opMode.gamepad2 = this.gamepad2
		opMode.hardwareMap = this.hardwareMap
		opMode.telemetry = this.telemetry
	}

	override fun init() {
		eventRegistrar.onOpModePreInit(this)
		opMode.init()
		eventRegistrar.onOpModePostInit(this)
	}

	override fun init_loop() {
		eventRegistrar.onOpModePreInitLoop(this)
		opMode.init_loop()
		eventRegistrar.onOpModePostInitLoop(this)
	}

	override fun start() {
		eventRegistrar.onOpModePreStart(this)
		opMode.start()
		eventRegistrar.onOpModePostStart(this)
	}

	override fun loop() {
		eventRegistrar.onOpModePreLoop(this)
		opMode.loop()
		eventRegistrar.onOpModePostLoop(this)
	}

	override fun stop() {
		eventRegistrar.onOpModePreStop(this)
		opMode.stop()
		eventRegistrar.onOpModePostStop(this)
	}
}
