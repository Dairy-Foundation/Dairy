package dev.frozenmilk.dairy.ftclink.calcified.hardware

import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoEnableCommand
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoPulseWidthCommand
import com.qualcomm.robotcore.hardware.PwmControl.PwmRange
import com.qualcomm.robotcore.util.Range
import dev.frozenmilk.dairy.ftclink.calcified.CalcifiedModule
import kotlin.math.abs

class CalcifiedServo internal constructor(private val module: CalcifiedModule, private val port: Byte) {

    var direction = Direction.FORWARD;

    var pwmRange: PwmRange = PwmRange(600.0, 2400.0)
    var cachingTolerance = 0.001

    var enabled = true
    set(value) {
        if (field != value) {
            LynxSetServoEnableCommand(module.lynxModule, port.toInt(), field).send()
            field = value;
        }
    }

    var pwmEnable = true
    set(value) {
        if(value != field) {
            LynxSetPWMEnableCommand(module.lynxModule, port.toInt(), field).send()
            field = value;
        }
    }

    var position = 0.0
    set(value) {
        if (value != field) {
            if ((abs(field - value) >= cachingTolerance) && pwmEnable) {
                val pwm = Range.scale(field, 0.0, 1.0, pwmRange.usPulseLower, pwmRange.usPulseUpper)
                LynxSetServoPulseWidthCommand(module.lynxModule, port.toInt(), pwm.toInt())
                field = value;
            }
        }
    }

}