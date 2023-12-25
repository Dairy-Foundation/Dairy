package dev.frozenmilk.dairy.core.util.cachinghardwaredevice;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ServoController;

public class CachingCRServo extends CachingDcMotorSimple implements CRServo {
	public final CRServo crServo;
	
	/**
	 * Default constructor for the cached continuous rotation servo, sets the threshold to 0.02
	 *
	 * @param CRServo the continuous rotation servo to encapsulate in the caching control
	 */
	
	public CachingCRServo(CRServo CRServo) {
		this(CRServo, 0.02);
	}
	
	/**
	 * Allows an initial setting of a custom changeThreshold
	 *
	 * @param CRServo the continuous rotation servo to encapsulate in the caching control
	 * @param changeThreshold the threshold at which the cache should write new values to the continuous rotation servo
	 */
	public CachingCRServo(CRServo CRServo, double changeThreshold) {
		super(CRServo, changeThreshold);
		this.crServo = CRServo;
	}
	
	/**
	 * Returns the underlying servo controller on which this servo is situated.
	 *
	 * @return the underlying servo controller on which this servo is situated.
	 * @see #getPortNumber()
	 */
	@Override
	public ServoController getController() {
		return crServo.getController();
	}
	
	/**
	 * Returns the port number on the underlying servo controller on which this motor is situated.
	 *
	 * @return the port number on the underlying servo controller on which this motor is situated.
	 * @see #getController()
	 */
	@Override
	public int getPortNumber() {
		return crServo.getPortNumber();
	}
}
