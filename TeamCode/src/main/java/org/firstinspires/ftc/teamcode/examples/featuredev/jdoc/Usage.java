package org.firstinspires.ftc.teamcode.examples.featuredev.jdoc;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.util.OpModeLazyCell;

// we add this, and BulkReads will receive updates for this OpMode
// which means it will handle bulk reads for us!
@BulkReads.Attach
// this annotation makes it so that the FeatureRegistrar will log all the reasons
// for any registered Features that weren't activated
@FeatureRegistrar.LogDependencyResolutionExceptions
public class Usage extends OpMode {
	public Usage() {
		// instead of `@FeatureRegistrar.LogDependencyResolutionExceptions`
		// checkFeatures can be used to ensure that all features
		// passed to the function will be activated,
		// or will throw an error for them specifically
		// both are good debugging tools!
		FeatureRegistrar.checkFeatures(BulkReads.INSTANCE/*, varargs Features*/);
	}
	
	// we'll look at OpModeLazyCell later, but this means that this PID will be instantiated in init for us
	// for this example it doesn't really matter, but if we actually implemented it, then we would need to use this
	// to ensure that we don't access the hardware map until init
	private final OpModeLazyCell<PID> pidCell = new OpModeLazyCell<>(PID::new);
	private PID getPID() { return pidCell.get(); }
	@Override
	public void init() {
	}
	
	// we can set the target in loop if we want
	// and we don't need to worry about anything else!
	@Override
	public void loop() {
		if (gamepad1.a) {
			getPID().setTarget(100);
		}
		else if (gamepad1.b) {
			getPID().setTarget(0);
		}
	}
}
