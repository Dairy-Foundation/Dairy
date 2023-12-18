package dev.frozenmilk.dairy.calcified.gamepad

import com.qualcomm.robotcore.hardware.Gamepad

class CalcifiedGamepad(private val gamepad: Gamepad) {
	/**
	 * left analog stick horizontal axis
	 */
	var leftStickX = EnhancedNumberSupplier { gamepad.left_stick_x.toDouble() }

	/**
	 * left analog stick vertical axis
	 */
	var leftStickY = EnhancedNumberSupplier { gamepad.left_stick_y.toDouble() }

	/**
	 * right analog stick horizontal axis
	 */
	var rightStickX = EnhancedNumberSupplier { gamepad.right_stick_x.toDouble() }

	/**
	 * right analog stick vertical axis
	 */
	var rightStickY = EnhancedNumberSupplier { gamepad.right_stick_y.toDouble() }

	/**
	 * dpad up
	 */
	var dpadUp = EnhancedBooleanSupplier { gamepad.dpad_up }

	/**
	 * dpad down
	 */
	var dpadDown = EnhancedBooleanSupplier { gamepad.dpad_down }

	/**
	 * dpad left
	 */
	var dpadLeft = EnhancedBooleanSupplier { gamepad.dpad_left }

	/**
	 * dpad right
	 */
	var dpadRight = EnhancedBooleanSupplier { gamepad.dpad_right }

	/**
	 * button a
	 */
	var a = EnhancedBooleanSupplier { gamepad.a }

	/**
	 * button b
	 */
	var b = EnhancedBooleanSupplier { gamepad.b }

	/**
	 * button x
	 */
	var x = EnhancedBooleanSupplier { gamepad.x }

	/**
	 * button y
	 */
	var y = EnhancedBooleanSupplier { gamepad.y }

	/**
	 * button guide - often the large button in the middle of the controller. The OS may
	 * capture this button before it is sent to the app; in which case you'll never
	 * receive it.
	 */
	var guide = EnhancedBooleanSupplier { gamepad.guide }

	/**
	 * button start
	 */
	var start = EnhancedBooleanSupplier { gamepad.start }

	/**
	 * button back
	 */
	var back = EnhancedBooleanSupplier { gamepad.back }

	/**
	 * button left bumper
	 */
	var leftBumper = EnhancedBooleanSupplier { gamepad.left_bumper }

	/**
	 * button right bumper
	 */
	var rightBumper = EnhancedBooleanSupplier { gamepad.right_bumper }

	/**
	 * left stick button
	 */
	var leftStickButton = EnhancedBooleanSupplier { gamepad.left_stick_button }

	/**
	 * right stick button
	 */
	var rightStickButton = EnhancedBooleanSupplier { gamepad.right_stick_button }

	/**
	 * left trigger
	 */
	var leftTrigger = EnhancedNumberSupplier { gamepad.left_trigger.toDouble() }

	/**
	 * right trigger
	 */
	var rightTrigger = EnhancedNumberSupplier { gamepad.right_trigger.toDouble() }

	/**
	 * PS4 Support - Circle
	 */
	var circle
		get() = b
		set(value) {
			b = value
		}

	/**
	 * PS4 Support - cross
	 */
	var cross
		get() = a
		set(value) {
			a = value
		}

	/**
	 * PS4 Support - triangle
	 */
	var triangle
		get() = y
		set(value) {
			y = value
		}

	/**
	 * PS4 Support - square
	 */
	var square
		get() = x
		set(value) {
			x = value
		}

	/**
	 * PS4 Support - share
	 */
	var share
		get() = back
		set(value) {
			back = value
		}

	/**
	 * PS4 Support - options
	 */
	var options
		get() = start
		set(value) {
			start = value
		}

	/**
	 * PS4 Support - touchpad
	 */
	var touchpad = EnhancedBooleanSupplier { gamepad.touchpad }

	var touchpadFinger1 = EnhancedBooleanSupplier { gamepad.touchpad_finger_1 }

	var touchpadFinger2 = EnhancedBooleanSupplier { gamepad.touchpad_finger_2 }

	var touchpadFinger1X = EnhancedNumberSupplier { gamepad.touchpad_finger_1_x.toDouble() }

	var touchpadFinger1Y = EnhancedNumberSupplier { gamepad.touchpad_finger_1_y.toDouble() }

	var touchpadFinger2X = EnhancedNumberSupplier { gamepad.touchpad_finger_2_x.toDouble() }

	var touchpadFinger2Y = EnhancedNumberSupplier { gamepad.touchpad_finger_2_y.toDouble() }

	/**
	 * PS4 Support - PS Button
	 */
	var ps
		get() = guide
		set(value) {
			guide = value
		}
}

