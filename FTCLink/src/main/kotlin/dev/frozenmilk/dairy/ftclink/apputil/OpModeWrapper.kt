package dev.frozenmilk.dairy.ftclink.apputil

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.DefaultOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import java.lang.reflect.Field
import java.util.Arrays

class OpModeWrapper(private val opMode: OpMode, private val eventRegistrar: FeatureRegistrar) : OpMode() {
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

	/**
	 * moves things around, so that the irritating little fields that exist on each OpMode get remapped through this correctly
	 *
	 * since this wrapper gets made AFTER the OpMode gets made and has a bunch of info passed to it, its mainly just pulling things up into this
	 */
	private fun initialiseThings() {
		this.gamepad1 = opMode.gamepad1
		this.gamepad2 = opMode.gamepad2

		val latest1 = OpMode::class.java.getDeclaredField("latestGamepad1Data")
		val latest2 = OpMode::class.java.getDeclaredField("latestGamepad2Data")

		latest1.isAccessible = true
		latest2.isAccessible = true

		latest1.set(this, latest1.get(opMode))
		latest2.set(this, latest2.get(opMode))

		//todo test these
		this.hardwareMap = opMode.hardwareMap
		this.telemetry = opMode.telemetry
		//todo test above

		opModeType // initialises the lazy property
	}

	override fun init() {
		initialiseThings();

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
