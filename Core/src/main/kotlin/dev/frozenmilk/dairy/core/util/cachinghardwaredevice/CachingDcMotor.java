package dev.frozenmilk.dairy.core.util.cachinghardwaredevice;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

public class CachingDcMotor extends CachingDcMotorSimple implements DcMotor {
	public final DcMotor dcMotor;
	private double cachedTargetPosition;
	
	/**
	 * Default constructor for the cached motor, sets the threshold to 0.02
	 *
	 * @param motor the motor to encapsulate in the caching control
	 */
	public CachingDcMotor(DcMotor motor) {
		super(motor);
		cachedTargetPosition = 0.0;
		this.dcMotor = motor;
	}
	
	/**
	 * Allows an initial setting of a custom changeThreshold
	 *
	 * @param motor the motor to encapsulate in the caching control
	 * @param changeThreshold the threshold at which the cache should write new values to the motor
	 */
	public CachingDcMotor(DcMotor motor, double changeThreshold) {
		super(motor, changeThreshold);
		cachedTargetPosition = 0.0;
		this.dcMotor = motor;
	}
	
	/**
	 * Returns the assigned type for this motor. If no particular motor type has been
	 * configured, then {@link MotorConfigurationType#getUnspecifiedMotorType()} will be returned.
	 * Note that the motor type for a given motor is initially assigned in the robot
	 * configuration user interface, though it may subsequently be modified using methods herein.
	 *
	 * @return the assigned type for this motor
	 */
	@Override
	public MotorConfigurationType getMotorType() {
		return dcMotor.getMotorType();
	}
	
	/**
	 * Sets the assigned type of this motor. Usage of this method is very rare.
	 *
	 * @param motorType the new assigned type for this motor
	 * @see #getMotorType()
	 */
	@Override
	public void setMotorType(MotorConfigurationType motorType) {
		dcMotor.setMotorType(motorType);
	}
	
	/**
	 * Returns the underlying motor controller on which this motor is situated.
	 *
	 * @return the underlying motor controller on which this motor is situated.
	 * @see #getPortNumber()
	 */
	@Override
	public DcMotorController getController() {
		return dcMotor.getController();
	}
	
	/**
	 * Returns the port number on the underlying motor controller on which this motor is situated.
	 *
	 * @return the port number on the underlying motor controller on which this motor is situated.
	 * @see #getController()
	 */
	@Override
	public int getPortNumber() {
		return dcMotor.getPortNumber();
	}
	
	/**
	 * Sets the behavior of the motor when a power level of zero is applied.
	 *
	 * @param zeroPowerBehavior the new behavior of the motor when a power level of zero is applied.
	 * @see ZeroPowerBehavior
	 * @see #setPower(double)
	 */
	@Override
	public void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {
		dcMotor.setZeroPowerBehavior(zeroPowerBehavior);
	}
	
	/**
	 * Returns the current behavior of the motor were a power level of zero to be applied.
	 *
	 * @return the current behavior of the motor were a power level of zero to be applied.
	 */
	@Override
	public ZeroPowerBehavior getZeroPowerBehavior() {
		return dcMotor.getZeroPowerBehavior();
	}
	
	/**
	 * Sets the zero power behavior of the motor to {@link ZeroPowerBehavior#FLOAT FLOAT}, then
	 * applies zero power to that motor.
	 *
	 * <p>Note that the change of the zero power behavior to {@link ZeroPowerBehavior#FLOAT FLOAT}
	 * remains in effect even following the return of this method. <STRONG>This is a breaking
	 * change</STRONG> in behavior from previous releases of the SDK. Consider, for example, the
	 * following code sequence:</p>
	 *
	 * <pre>
	 *     motor.setZeroPowerBehavior(ZeroPowerBehavior.BRAKE); // method not available in previous releases
	 *     motor.setPowerFloat();
	 *     motor.setPower(0.0);
	 * </pre>
	 *
	 * <p>Starting from this release, this sequence of code will leave the motor floating. Previously,
	 * the motor would have been left braked.</p>
	 *
	 * @see #setPower(double)
	 * @see #getPowerFloat()
	 * @see #setZeroPowerBehavior(ZeroPowerBehavior)
	 * @deprecated This method is deprecated in favor of direct use of
	 * {@link #setZeroPowerBehavior(ZeroPowerBehavior) setZeroPowerBehavior()} and
	 * {@link #setPower(double) setPower()}.
	 */
	@Override
	@Deprecated
	public void setPowerFloat() {
		dcMotor.setPowerFloat();
	}
	
	/**
	 * Returns whether the motor is currently in a float power level.
	 *
	 * @return whether the motor is currently in a float power level.
	 * @see #setPowerFloat()
	 */
	@Override
	@Deprecated
	public boolean getPowerFloat() {
		return dcMotor.getPowerFloat();
	}
	
	/**
	 * Sets the desired encoder target position to which the motor should advance or retreat
	 * and then actively hold thereat. This behavior is similar to the operation of a servo.
	 * The maximum speed at which this advance or retreat occurs is governed by the power level
	 * currently set on the motor. While the motor is advancing or retreating to the desired
	 * taget position, {@link #isBusy()} will return true.
	 *
	 * <p>Note that adjustment to a target position is only effective when the motor is in
	 * {@link RunMode#RUN_TO_POSITION RUN_TO_POSITION}
	 * RunMode. Note further that, clearly, the motor must be equipped with an encoder in order
	 * for this mode to function properly.</p>
	 *
	 * @param position the desired encoder target position
	 * @see #getCurrentPosition()
	 * @see #setMode(RunMode)
	 * @see RunMode#RUN_TO_POSITION
	 * @see #getTargetPosition()
	 * @see #isBusy()
	 */
	@Override
	public void setTargetPosition(int position) {
		if(Math.abs(cachedTargetPosition - position) >= changeThreshold) {
			dcMotor.setTargetPosition(position);
			cachedTargetPosition = position;
		}
	}
	
	/**
	 * Returns the current target encoder position for this motor.
	 *
	 * @return the current target encoder position for this motor.
	 * @see #setTargetPosition(int)
	 */
	@Override
	public int getTargetPosition() {
		return dcMotor.getTargetPosition();
	}
	
	/**
	 * Returns true if the motor is currently advancing or retreating to a target position.
	 *
	 * @return true if the motor is currently advancing or retreating to a target position.
	 * @see #setTargetPosition(int)
	 */
	@Override
	public boolean isBusy() {
		return dcMotor.isBusy();
	}
	
	/**
	 * Returns the current reading of the encoder for this motor. The units for this reading,
	 * that is, the number of ticks per revolution, are specific to the motor/encoder in question,
	 * and thus are not specified here.
	 *
	 * @return the current reading of the encoder for this motor
	 * @see #getTargetPosition()
	 * @see RunMode#STOP_AND_RESET_ENCODER
	 */
	@Override
	public int getCurrentPosition() {
		return dcMotor.getCurrentPosition();
	}
	
	/**
	 * Sets the current run mode for this motor
	 *
	 * @param mode the new current run mode for this motor
	 * @see RunMode
	 * @see #getMode()
	 */
	@Override
	public void setMode(RunMode mode) {
		dcMotor.setMode(mode);
	}
	
	/**
	 * Returns the current run mode for this motor
	 *
	 * @return the current run mode for this motor
	 * @see RunMode
	 * @see #setMode(RunMode)
	 */
	@Override
	public RunMode getMode() {
		return dcMotor.getMode();
	}
}
