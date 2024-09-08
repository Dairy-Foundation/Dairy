package org.firstinspires.ftc.teamcode.examples.sinister

import dev.frozenmilk.sinister.Preload

// This is from Sinister, this means that any class that implements this
// interface will be `preloaded` this means we can look for static instances in
// these classes
@Preload
@FunctionalInterface
fun interface KotlinEventReceiver {
	fun receiveEvent(eventLevel: Int, message: String)
}