package dev.frozenmilk.dairy.core.util.cachinghardwaredevice;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class CachingDcMotorEX extends CachingDcMotor implements DcMotorEx{
	public final DcMotorEx motorEx;
	/**
	 * Default constructor for the cached motorEx, sets the threshold to 0.02
	 *
	 * @param motorEx the motor to encapsulate in the caching control
	 */
	public CachingDcMotorEX(DcMotorEx motorEx) {
		super(motorEx);
		this.motorEx = motorEx;
	}
	
	/**
	 * Allows an initial setting of a custom changeThreshold
	 *
	 * @param motorEx the motor to encapsulate in the caching control
	 * @param changeThreshold the threshold at which the cache should write new values to the motor
	 */
	public CachingDcMotorEX(DcMotorEx motorEx, double changeThreshold) {
		super(motorEx, changeThreshold);
		this.motorEx = motorEx;
	}
	
	/**
	 * Individually energizes this particular motor
	 *
	 * @see #setMotorDisable()
	 * @see #isMotorEnabled()
	 */
	@Override
	public void setMotorEnable() {
		motorEx.setMotorEnable();
	}
	
	/**
	 * Individually de-energizes this particular motor
	 *
	 * @see #setMotorEnable()
	 * @see #isMotorEnabled()
	 */
	@Override
	public void setMotorDisable() {
		motorEx.setMotorDisable();
	}
	
	/**
	 * Returns whether this motor is energized
	 *
	 * @see #setMotorEnable()
	 * @see #setMotorDisable()
	 */
	@Override
	public boolean isMotorEnabled() {
		return motorEx.isMotorEnabled();
	}
	
	/**
	 * Sets the velocity of the motor
	 *
	 * @param angularRate the desired ticks per second
	 */
	@Override
	public void setVelocity(double angularRate) {
		motorEx.setVelocity(angularRate);
	}
	
	/**
	 * Sets the velocity of the motor
	 *
	 * @param angularRate the desired angular rate, in units per second
	 * @param unit        the units in which angularRate is expressed
	 * @see #getVelocity(AngleUnit)
	 */
	@Override
	public void setVelocity(double angularRate, AngleUnit unit) {
		motorEx.setVelocity(angularRate, unit);
	}
	
	/**
	 * Returns the current velocity of the motor, in ticks per second
	 *
	 * @return the current velocity of the motor
	 */
	@Override
	public double getVelocity() {
		return motorEx.getVelocity();
	}
	
	/**
	 * Returns the current velocity of the motor, in angular units per second
	 *
	 * @param unit the units in which the angular rate is desired
	 * @return the current velocity of the motor
	 * @see #setVelocity(double, AngleUnit)
	 */
	@Override
	public double getVelocity(AngleUnit unit) {
		return motorEx.getVelocity(unit);
	}
	
	/**
	 * Sets the PID control coefficients for one of the PID modes of this motor.
	 * Note that in some controller implementations, setting the PID coefficients for one
	 * mode on a motor might affect other modes on that motor, or might affect the PID
	 * coefficients used by other motors on the same controller (this is not true on the
	 * REV Expansion Hub).
	 *
	 * @param mode            either {@link RunMode#RUN_USING_ENCODER} or {@link RunMode#RUN_TO_POSITION}
	 * @param pidCoefficients the new coefficients to use when in that mode on this motor
	 * @see #getPIDCoefficients(RunMode)
	 * @deprecated Use {@link #setPIDFCoefficients(RunMode, PIDFCoefficients)} instead
	 */
	@Override
	@Deprecated
	public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {
		motorEx.setPIDCoefficients(mode, pidCoefficients);
	}
	
	/**
	 * {@link #setPIDFCoefficients} is a superset enhancement to {@link #setPIDCoefficients}. In addition
	 * to the proportional, integral, and derivative coefficients previously supported, a feed-forward
	 * coefficient may also be specified. Further, a selection of motor control algorithms is offered:
	 * the originally-shipped Legacy PID algorithm, and a PIDF algorithm which avails itself of the
	 * feed-forward coefficient. Note that the feed-forward coefficient is not used by the Legacy PID
	 * algorithm; thus, the feed-forward coefficient must be indicated as zero if the Legacy PID
	 * algorithm is used. Also: the internal implementation of these algorithms may be different: it
	 * is not the case that the use of PIDF with the F term as zero necessarily exhibits exactly the
	 * same behavior as the use of the LegacyPID algorithm, though in practice they will be quite close.
	 * <p>
	 * Readers are reminded that {@link DcMotor.RunMode#RUN_TO_POSITION} mode makes use of <em>both</em>
	 * the coefficients set for RUN_TO_POSITION <em>and</em> the coefficients set for RUN_WITH_ENCODER,
	 * due to the fact that internally the RUN_TO_POSITION logic calculates an on-the-fly velocity goal
	 * on each control cycle, then (logically) runs the RUN_WITH_ENCODER logic. Because of that double-
	 * layering, only the proportional ('p') coefficient makes logical sense for use in the RUN_TO_POSITION
	 * coefficients.
	 *
	 * @param mode
	 * @param pidfCoefficients
	 * @see #setVelocityPIDFCoefficients(double, double, double, double)
	 * @see #setPositionPIDFCoefficients(double)
	 * @see #getPIDFCoefficients(RunMode)
	 */
	@Override
	public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) throws UnsupportedOperationException {
		motorEx.setPIDFCoefficients(mode, pidfCoefficients);
	}
	
	/**
	 * A shorthand for setting the PIDF coefficients for the {@link DcMotor.RunMode#RUN_USING_ENCODER}
	 * mode. {@link MotorControlAlgorithm#PIDF} is used.
	 *
	 * @param p
	 * @param i
	 * @param d
	 * @param f
	 * @see #setPIDFCoefficients(RunMode, PIDFCoefficients)
	 */
	@Override
	public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {
		motorEx.setVelocityPIDFCoefficients(p, i, d, f);
	}
	
	/**
	 * A shorthand for setting the PIDF coefficients for the {@link DcMotor.RunMode#RUN_TO_POSITION}
	 * mode. {@link MotorControlAlgorithm#PIDF} is used.
	 * <p>
	 * Readers are reminded that {@link DcMotor.RunMode#RUN_TO_POSITION} mode makes use of <em>both</em>
	 * the coefficients set for RUN_TO_POSITION <em>and</em> the coefficients set for RUN_WITH_ENCODER,
	 * due to the fact that internally the RUN_TO_POSITION logic calculates an on-the-fly velocity goal
	 * on each control cycle, then (logically) runs the RUN_WITH_ENCODER logic. Because of that double-
	 * layering, only the proportional ('p') coefficient makes logical sense for use in the RUN_TO_POSITION
	 * coefficients.
	 *
	 * @param p
	 * @see #setVelocityPIDFCoefficients(double, double, double, double)
	 * @see #setPIDFCoefficients(RunMode, PIDFCoefficients)
	 */
	@Override
	public void setPositionPIDFCoefficients(double p) {
		motorEx.setPositionPIDFCoefficients(p);
	}
	
	/**
	 * Returns the PID control coefficients used when running in the indicated mode
	 * on this motor.
	 *
	 * @param mode either {@link RunMode#RUN_USING_ENCODER} or {@link RunMode#RUN_TO_POSITION}
	 * @return the PID control coefficients used when running in the indicated mode on this motor
	 * @deprecated Use {@link #getPIDFCoefficients(RunMode)} instead
	 */
	@Override
	@Deprecated
	public PIDCoefficients getPIDCoefficients(RunMode mode) {
		return motorEx.getPIDCoefficients(mode);
	}
	
	/**
	 * Returns the PIDF control coefficients used when running in the indicated mode
	 * on this motor.
	 *
	 * @param mode either {@link RunMode#RUN_USING_ENCODER} or {@link RunMode#RUN_TO_POSITION}
	 * @return the PIDF control coefficients used when running in the indicated mode on this motor
	 * @see #setPIDFCoefficients(RunMode, PIDFCoefficients)
	 */
	@Override
	public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
		return motorEx.getPIDFCoefficients(mode);
	}
	
	/**
	 * Sets the target positioning tolerance of this motor
	 *
	 * @param tolerance the desired tolerance, in encoder ticks
	 * @see DcMotor#setTargetPosition(int)
	 */
	@Override
	public void setTargetPositionTolerance(int tolerance) {
		motorEx.setTargetPositionTolerance(tolerance);
	}
	
	/**
	 * Returns the current target positioning tolerance of this motor
	 *
	 * @return the current target positioning tolerance of this motor
	 */
	@Override
	public int getTargetPositionTolerance() {
		return motorEx.getTargetPositionTolerance();
	}
	
	/**
	 * Returns the current consumed by this motor.
	 *
	 * @param unit current units
	 * @return the current consumed by this motor.
	 */
	@Override
	public double getCurrent(CurrentUnit unit) {
		return motorEx.getCurrent(unit);
	}
	
	/**
	 * Returns the current alert for this motor.
	 *
	 * @param unit current units
	 * @return the current alert for this motor
	 */
	@Override
	public double getCurrentAlert(CurrentUnit unit) {
		return motorEx.getCurrentAlert(unit);
	}
	
	/**
	 * Sets the current alert for this motor
	 *
	 * @param current current alert
	 * @param unit    current units
	 */
	@Override
	public void setCurrentAlert(double current, CurrentUnit unit) {
		motorEx.setCurrentAlert(current, unit);
	}
	
	/**
	 * Returns whether the current consumption of this motor exceeds the alert threshold.
	 *
	 * @return whether the current consumption of this motor exceeds the alert threshold.
	 */
	@Override
	public boolean isOverCurrent() {
		return motorEx.isOverCurrent();
	}
}
