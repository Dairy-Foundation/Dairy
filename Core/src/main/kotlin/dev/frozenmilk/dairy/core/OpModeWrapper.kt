package dev.frozenmilk.dairy.core

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor

class OpModeWrapper internal constructor(private val opMode: OpMode, val meta: OpModeMeta) : OpMode() {
	val opModeType: Flavor = meta.flavor

	enum class OpModeState {
		/**
		 * in [OpMode.init] or [OpMode.init_loop]
		 */
		INIT,

		/**
		 * in [OpMode.start], [OpMode.loop] or [OpMode.stop]
		 */
		ACTIVE,

		/**
		 * inactive
		 */
		STOPPED,
	}

	var state: OpModeState = OpModeState.STOPPED
		internal set

	val name = meta.displayName

	/**
	 * moves things around, so that the irritating little fields that exist on each OpMode get remapped through this correctly
	 *
	 * since this wrapper gets made AFTER the OpMode gets made and has a bunch of info passed to it, its mainly just pulling things up into this
	 */
	internal fun initialiseThings() {
		this.gamepad1 = opMode.gamepad1
		this.gamepad2 = opMode.gamepad2

		val latest1 = OpMode::class.java.getDeclaredField("latestGamepad1Data")
		val latest2 = OpMode::class.java.getDeclaredField("latestGamepad2Data")

		latest1.isAccessible = true
		latest2.isAccessible = true

		latest1.set(this, latest1.get(opMode))
		latest2.set(this, latest2.get(opMode))

		this.hardwareMap = opMode.hardwareMap
		this.telemetry = opMode.telemetry

		opModeType // initialises the lazy property
		state = OpModeState.INIT
	}

	override fun init() {
		FeatureRegistrar.onOpModePreInit(this)
		opMode.init()
		FeatureRegistrar.onOpModePostInit(this)
	}

	override fun init_loop() {
		FeatureRegistrar.onOpModePreInitLoop(this)
		opMode.init_loop()
		FeatureRegistrar.onOpModePostInitLoop(this)
	}

	override fun start() {
		FeatureRegistrar.onOpModePreStart(this)
		opMode.start()
		FeatureRegistrar.onOpModePostStart(this)
	}

	override fun loop() {
		FeatureRegistrar.onOpModePreLoop(this)
		opMode.loop()
		FeatureRegistrar.onOpModePostLoop(this)
	}

	override fun stop() {
		FeatureRegistrar.onOpModePreStop(this)
		opMode.stop()
		FeatureRegistrar.onOpModePostStop(this)
	}
}
