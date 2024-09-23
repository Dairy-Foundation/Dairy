package org.firstinspires.ftc.teamcode.examples.templating

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.util.OpModeLazyCell
import dev.frozenmilk.mercurial.Mercurial
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.teamcode.examples.mercurial.JavaSubsystem
import org.firstinspires.ftc.teamcode.examples.mercurial.KotlinSubsystem

// attach annotations can be applied to a parent in the inheritance tree!
// this way you can set up re-usable attachment annotation configurations
// these annotations are not particularly relevant to this example, but demonstrate the idea
@Mercurial.Attach
@KotlinSubsystem.Attach
@JavaSubsystem.Attach
// to perform further configurations of features, write a feature that runs after them:
@KotlinConfigurationFeature.Attach
abstract class KotlinTemplate : OpMode() {
	// or use a private OpModeLazy cell that gets run later:
	private val _init = OpModeLazyCell {
		// control default commands to be turned off for AUTO, but be set up for TELEOP
		// this would be better done in the subsystems themselves,
		// but this is also fine
		when(FeatureRegistrar.activeOpModeWrapper.opModeType) {
			OpModeMeta.Flavor.AUTONOMOUS -> {
				KotlinSubsystem.defaultCommand = null
				JavaSubsystem.INSTANCE.defaultCommand = null
			}
			OpModeMeta.Flavor.TELEOP -> {
				KotlinSubsystem.defaultCommand = KotlinSubsystem.statefulCommand()
				JavaSubsystem.INSTANCE.defaultCommand = JavaSubsystem.statefulCommand()
			}
			OpModeMeta.Flavor.SYSTEM -> {}
		}
		// while a OpModeLazyCell could be used to add setup configuration,
		// it may not work for everything.
		// e.g., lazily configuring Mercurial gamepads,
		// or if our configuration NEEDS to run after a certain feature runs its own init code
		// for this, a full feature is needed
	}

	// each of these will be set up in init, so we don't need to manually call any of it
	val leftBack by OpModeLazyCell {
		hardwareMap.get(DcMotor::class.java, "leftBack")
	}
	val leftFront by OpModeLazyCell {
		hardwareMap.get(DcMotor::class.java, "leftFront")
	}
	val rightBack by OpModeLazyCell {
		hardwareMap.get(DcMotor::class.java, "rightBack")
	}
	val rightFront by OpModeLazyCell {
		hardwareMap.get(DcMotor::class.java, "rightFront")
	}
}