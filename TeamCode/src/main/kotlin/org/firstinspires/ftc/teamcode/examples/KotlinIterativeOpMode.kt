package org.firstinspires.ftc.teamcode.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.dairy.core.FeatureRegistrar

// add feature annotations here
class KotlinIterativeOpMode : OpMode() {
	init {
		FeatureRegistrar.checkFeatures(this, /* pass desired features as varargs here */)
	}

	override fun init() {
		// the rest is as normal
		// remember that you can use OpModeLazyCells to init your hardware and similar
	}

	override fun init_loop() {
		// the rest is as normal
	}

	override fun start() {
		// the rest is as normal
	}

	override fun loop() {
		// the rest is as normal
	}

	override fun stop() {
		// the rest is as normal
	}
}