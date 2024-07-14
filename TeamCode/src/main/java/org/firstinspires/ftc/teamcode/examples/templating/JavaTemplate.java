package org.firstinspires.ftc.teamcode.examples.templating;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.examples.mercurial.JavaSubsystem;
import org.firstinspires.ftc.teamcode.examples.mercurial.KotlinSubsystem;

import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.util.OpModeLazyCell;
import dev.frozenmilk.mercurial.Mercurial;

// attach annotations can be applied to a parent in the inheritance tree!
// this way you can set up re-usable attachment annotation configurations
// these annotations are not particularly relevant to this example, but demonstrate the idea
@Mercurial.Attach
@KotlinSubsystem.Attach
@JavaSubsystem.Attach
// to perform further configurations of features, write a feature that runs after them:
@JavaConfigurationFeature.Attach
public abstract class JavaTemplate extends OpMode {
	// or use a private OpModeLazy cell that gets run later:
	private final OpModeLazyCell<?> _init = new OpModeLazyCell<>(() -> {
		// control default commands to be turned off for AUTO, but be set up for TELEOP
		// this would be better done in the subsystems themselves,
		// but this is also fine
		switch (FeatureRegistrar.getActiveOpModeWrapper().getOpModeType()) {
			case AUTONOMOUS:
				KotlinSubsystem.INSTANCE.setDefaultCommand(null);
				JavaSubsystem.getInstance().setDefaultCommand(null);
				break;
			case TELEOP:
				KotlinSubsystem.INSTANCE.setDefaultCommand(KotlinSubsystem.INSTANCE.statefulCommand());
				JavaSubsystem.getInstance().setDefaultCommand(JavaSubsystem.statefulCommand());
				break;
		}
		return null;
		// while a OpModeLazyCell could be used to add setup configuration,
		// it may not work for everything.
		// e.g., lazily configuring Mercurial gamepads,
		// or if our configuration NEEDS to run after a certain feature runs its own init code
		// for this, a full feature is needed
	});
	
	// each of these will be set up in init, so we don't need to manually call any of it
	
	private final OpModeLazyCell<DcMotor> leftBackCell =
			new OpModeLazyCell<>(() -> hardwareMap.get(DcMotor.class, "leftBack"));
	public DcMotor getLeftBack() { return leftBackCell.get(); }
	
	private final OpModeLazyCell<DcMotor> leftFrontCell =
			new OpModeLazyCell<>(() -> hardwareMap.get(DcMotor.class, "leftFront"));
	public DcMotor getLeftFront() { return leftFrontCell.get(); }
	
	private final OpModeLazyCell<DcMotor> rightBackCell =
			new OpModeLazyCell<>(() -> hardwareMap.get(DcMotor.class, "rightBack"));
	public DcMotor getRightBack() { return rightBackCell.get(); }
	
	private final OpModeLazyCell<DcMotor> rightFrontCell =
			new OpModeLazyCell<>(() -> hardwareMap.get(DcMotor.class, "rightFront"));
	public DcMotor getRightFront() { return rightFrontCell.get(); }
}
