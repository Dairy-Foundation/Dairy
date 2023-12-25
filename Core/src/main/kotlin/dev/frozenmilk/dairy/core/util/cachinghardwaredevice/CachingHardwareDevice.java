package dev.frozenmilk.dairy.core.util.cachinghardwaredevice;

import com.qualcomm.robotcore.hardware.HardwareDevice;

public abstract class CachingHardwareDevice implements HardwareDevice {
	public final HardwareDevice hardwareDevice;
	protected CachingHardwareDevice(HardwareDevice hardwareDevice) {
		this.hardwareDevice = hardwareDevice;
	}
	
	public HardwareDevice getHardwareDevice() {
		return this.hardwareDevice;
	}
	
	/**
	 * Returns an indication of the manufacturer of this device.
	 * @return the device's manufacturer
	 */
	@Override
	public Manufacturer getManufacturer() {
		return this.hardwareDevice.getManufacturer();
	}
	
	/**
	 * Returns a string suitable for display to the user as to the type of device.
	 * Note that this is a device-type-specific name; it has nothing to do with the
	 * name by which a user might have configured the device in a robot configuration.
	 *
	 * @return device manufacturer and name
	 */
	@Override
	public String getDeviceName() {
		return this.hardwareDevice.getDeviceName();
	}
	
	/**
	 * Get connection information about this device in a human readable format
	 *
	 * @return connection info
	 */
	@Override
	public String getConnectionInfo() {
		return this.hardwareDevice.getConnectionInfo();
	}
	
	/**
	 * Version
	 *
	 * @return get the version of this device
	 */
	@Override
	public int getVersion() {
		return this.hardwareDevice.getVersion();
	}
	
	/**
	 * Resets the device's configuration to that which is expected at the beginning of an OpMode.
	 * For example, motors will reset the their direction to 'forward'.
	 */
	@Override
	public void resetDeviceConfigurationForOpMode() {
		this.hardwareDevice.resetDeviceConfigurationForOpMode();
	}
	
	/**
	 * Closes this device
	 */
	@Override
	public void close() {
		this.hardwareDevice.close();
	}
}
