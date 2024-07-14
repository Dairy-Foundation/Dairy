package org.firstinspires.ftc.teamcode.examples.templating

import com.qualcomm.robotcore.eventloop.opmode.TeleOp

@TeleOp
// because we are extending KotlinTemplate, we get everything we set up before!
class KotlinTemplatedOpMode : KotlinTemplate() {
	override fun init() {
	}

	override fun loop() {
		leftBack.power = 1.0
		// etc...
	}
}