package dev.frozenmilk.dairy.ftclink.apputil

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp

class OpModeWrapper(private val opMode: OpMode, private val eventRegistrar: EventRegistrar) : OpMode() {
	enum class OpModeType {
		TELEOP,
		AUTONOMOUS,
		NONE
	}

	private val opModeType: OpModeType by lazy {
		if (opMode.javaClass.isAnnotationPresent(TeleOp::class.java)) OpModeType.TELEOP
		else if (opMode.javaClass.isAnnotationPresent(Autonomous::class.java)) OpModeType.AUTONOMOUS
		else OpModeType.NONE
	}

	init {
		// may allow passthrough to these user accessed values
		opMode.gamepad1 = this.gamepad1
		opMode.gamepad2 = this.gamepad2
		opMode.hardwareMap = this.hardwareMap
		opMode.telemetry = this.telemetry
		opModeType // initialises the lazy property
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
