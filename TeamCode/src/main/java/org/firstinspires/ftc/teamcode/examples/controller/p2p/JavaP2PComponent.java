package org.firstinspires.ftc.teamcode.examples.controller.p2p;

import androidx.annotation.NonNull;

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents;
import dev.frozenmilk.util.units.angle.AngleUnits;
import dev.frozenmilk.util.units.angle.Wrapping;
import dev.frozenmilk.util.units.distance.DistanceUnits;
import dev.frozenmilk.util.units.position.DistancePose2D;

public class JavaP2PComponent implements ControllerCalculation<DistancePose2D> {
	private final double kP, kD;
	private final DistancePose2D zero = new DistancePose2D();
	private DistancePose2D previousError = zero;
	public JavaP2PComponent(double kP, double kD) {
		this.kP = kP;
		this.kD = kD;
	}
	@Override
	public void update(
			@NonNull DistancePose2D accumulation,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> state,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> target,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> error,
			double deltaTime
	) {
		previousError = error.get(MotionComponents.STATE).into(DistanceUnits.MILLIMETER, DistanceUnits.MILLIMETER, AngleUnits.RADIAN, Wrapping.LINEAR);
	}
	
	@NonNull
	@Override
	public DistancePose2D evaluate(
			@NonNull DistancePose2D accumulation,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> state,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> target,
			@NonNull MotionComponentSupplier<? extends DistancePose2D> error,
			double deltaTime
	) {
		DistancePose2D res = accumulation;
		DistancePose2D err = error.get(MotionComponents.STATE).into(DistanceUnits.MILLIMETER, DistanceUnits.MILLIMETER, AngleUnits.RADIAN, Wrapping.LINEAR);
		DistancePose2D resP = err.times(kP);
		DistancePose2D resD = ((err.minus(previousError)).div(deltaTime)).times(kD);
		previousError = err;
		if (!(resP.getVector2D().getX().isNaN() || resP.getVector2D().getY().isNaN() || resP.getHeading().isNaN())) res = res.plus(resP);
		if (!(resD.getVector2D().getX().isNaN() || resD.getVector2D().getY().isNaN() || resD.getHeading().isNaN())) res = res.plus(resD);
		return res;
	}
	
	@Override
	public void reset() {
		previousError = zero;
	}
}
