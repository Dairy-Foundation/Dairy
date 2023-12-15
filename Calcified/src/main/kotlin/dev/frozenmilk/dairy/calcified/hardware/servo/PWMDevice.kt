package dev.frozenmilk.dairy.calcified.hardware.servo

import com.qualcomm.robotcore.hardware.PwmControl

interface PWMDevice {
	var pwmRange: PwmControl.PwmRange
}