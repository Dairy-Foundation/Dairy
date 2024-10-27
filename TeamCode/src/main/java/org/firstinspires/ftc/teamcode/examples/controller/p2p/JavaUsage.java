package org.firstinspires.ftc.teamcode.examples.controller.p2p;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import dev.frozenmilk.dairy.core.util.OpModeLazyCell;
import dev.frozenmilk.dairy.core.util.controller.implementation.DistancePoseController;
import dev.frozenmilk.dairy.core.util.controller.implementation.MotionComponentConsumer;
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents;
import dev.frozenmilk.util.units.angle.Angles;
import dev.frozenmilk.util.units.distance.Distances;
import dev.frozenmilk.util.units.position.DistancePose2D;
import dev.frozenmilk.util.units.position.DistancePose2Ds;

public class JavaUsage extends OpMode {
	private final OpModeLazyCell<JavaThreeWheelLocalizer> localizerCell = new OpModeLazyCell<>(() ->
			new JavaThreeWheelLocalizer(
					Distances.cm(0),
					Distances.cm(0),
					0,
					0,
					null, // left encoder
					null, // right encoder
					null  // middle encoder
			)
	);
	private final DistancePose2D zero = new DistancePose2D();
	private DistancePose2D target = new DistancePose2D();
	private final MotionComponentSupplier<DistancePose2D> targetSupplier = motionComponent -> {
		if (motionComponent == MotionComponents.STATE) {
			return target;
		}
		return zero;
	};
	private final OpModeLazyCell<DistancePoseController> p2pCell = new OpModeLazyCell<>(() ->
			new DistancePoseController(
					targetSupplier, // target
					localizerCell.get(),
					new CachedMotionComponentSupplier<DistancePose2D>(motionComponent -> {
						if (motionComponent == MotionComponents.STATE) {
							return DistancePose2Ds.millimeterPose(50, 50, Angles.linearDeg(10).intoRadians());
						}
						else if (motionComponent == MotionComponents.VELOCITY) {
							return DistancePose2Ds.millimeterPose(10, 10, Angles.linearDeg(1).intoRadians());
						}
						return zero;
					}),
					new JavaP2PComponent(0.001, 0.00005)
			)
	);
	@Override
	public void init() {
	
	}
	
	@Override
	public void loop() {
		// set target
		target = new DistancePose2D();
	}
}
