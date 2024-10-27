package org.firstinspires.ftc.teamcode.examples.controller.p2p;

import androidx.annotation.NonNull;

import java.util.function.Supplier;

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier;
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier;
import dev.frozenmilk.util.units.VelocityPacketKt;
import dev.frozenmilk.util.units.angle.Angles;
import dev.frozenmilk.util.units.distance.Distance;
import dev.frozenmilk.util.units.position.DistancePose2D;
import dev.frozenmilk.util.units.position.DistanceVector2D;

public class JavaThreeWheelLocalizer extends EnhancedNumericSupplier<DistancePose2D> {
	private Distance prevLeftState;
	private Distance prevRightState;
	private Distance prevMiddleState;
	private final Distance trackWidth;
	private final Distance forwardOffset;
	private final double xMult, yMult;
	private final EnhancedUnitSupplier<Distance> left;
	private final EnhancedUnitSupplier<Distance> right;
	private final EnhancedUnitSupplier<Distance> middle;
	private final DistancePose2D zero = new DistancePose2D();
	private DistancePose2D offset = zero;
	private DistancePose2D currentState = zero;
	public JavaThreeWheelLocalizer(
			Distance trackWidth,
			Distance forwardOffset,
			double xMult,
			double yMult,
			@NonNull EnhancedUnitSupplier<Distance> left,
			@NonNull EnhancedUnitSupplier<Distance> right,
			@NonNull EnhancedUnitSupplier<Distance> middle
	) {
		this.prevLeftState = left.state();
		this.prevRightState = right.state();
		this.prevMiddleState = middle.state();
		this.xMult = xMult;
		this.yMult = yMult;
		this.left = left;
		this.right = right;
		this.middle = middle;
		this.trackWidth = trackWidth;
		this.forwardOffset = forwardOffset;
	}
	
	private DistancePose2D calc() {
		Distance dLeft = left.state().minus(prevLeftState).intoCommon();
		Distance dRight = right.state().minus(prevRightState).intoCommon();
		Distance dMiddle = left.state().minus(prevMiddleState).intoCommon();
		
		Distance dTheta = dLeft.minus(dRight).div(trackWidth).intoCommon();
		Distance dCenter = dLeft.plus(dRight).div(2).intoCommon();
		Distance dPerpendicular = dMiddle.plus(dTheta.times(forwardOffset)).intoCommon();
		
		double cos = currentState.getHeading().getCos();
		double sin = currentState.getHeading().getSin();
		
		double term0 = (Math.sin(dTheta.getValue()) / dTheta.getValue()); // approaches 1 as dt approaches 0
		double term1 = (1 - Math.cos(dTheta.getValue())) / dTheta.getValue(); // approaches 0 as dt approaches 0
		
		if (dTheta.getValue() == 0) {
			term0 = 1; // approaches 1 as dt approaches 0
			term1 = 0; // approaches 0 as dt approaches 0
		}
		
		/*
		{
			{(cos * term0 + (- sin) * term1), (cos * term2 + (- sin) * term0)},
			{(sin * term0 + cos * term1), (sin * term2 + cos * term0)}
		}

		{
			dc * (cos * term0 + (- sin) * term1) + dp * (cos * term2 + (- sin) * term0),
			dc * (sin * term0 + cos * term1) + dp * (sin * term2 + cos * term0)}
		}
		*/
		
		prevLeftState = left.state();
		prevRightState = right.state();
		prevMiddleState = middle.state();
		
		return currentState.plus(new DistancePose2D(
				new DistanceVector2D(
						dCenter.times(cos * term0 + (-sin) * term1).plus(dPerpendicular.times(cos * (-term1) + (-sin) * term0)).times(xMult),
						dCenter.times(sin * term0 + cos * term1).plus(dPerpendicular.times(sin * (-term1) + cos * term0)).times(yMult)
				),
				Angles.linearRad(dTheta.getValue())
		));
	}
	
	@NonNull
	@Override
	public Supplier<? extends DistancePose2D> getSupplier() {
		return this::calc;
	}
	
	@Override
	protected DistancePose2D getZero() {
		return zero;
	}
	
	@Override
	protected DistancePose2D getCurrentState() {
		return currentState;
	}
	
	@Override
	protected void setCurrentState(DistancePose2D distancePose2D) {
		this.currentState = distancePose2D;
	}
	
	@Override
	public DistancePose2D state() {
		return get().minus(offset);
	}
	
	@Override
	public void state(DistancePose2D distancePose2D) {
		offset = currentState.minus(distancePose2D);
	}
	
	@Override
	public DistancePose2D velocity() {
		return VelocityPacketKt.getVelocity(VelocityPacketKt.homogenise(getPreviousVelocities()));
	}
	
	@Override
	public DistancePose2D rawVelocity() {
		return VelocityPacketKt.getVelocity(getPreviousPositions().last());
	}
	
	@Override
	public DistancePose2D acceleration() {
		return VelocityPacketKt.getVelocity(VelocityPacketKt.homogenise(getPreviousVelocities()));
	}
	
	@Override
	public DistancePose2D rawAcceleration() {
		return VelocityPacketKt.getVelocity(getPreviousVelocities().last());
	}
}
