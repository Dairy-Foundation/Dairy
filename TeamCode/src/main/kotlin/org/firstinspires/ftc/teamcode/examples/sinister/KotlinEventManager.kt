package org.firstinspires.ftc.teamcode.examples.sinister

import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import java.util.function.Consumer

object KotlinEventManager {
	// we'll make it possible to publish an event from anywhere
	fun publishEvent(eventLevel: Int, message: String?) {
		EventReceiverFilter.eventReceivers.forEach(
			Consumer { javaEventReceiver: JavaEventReceiver ->
				javaEventReceiver.receiveEvent(
					eventLevel,
					message
				)
			}
		)
	}
	// this will collect the instances of EventReceiver
	private object EventReceiverFilter : SinisterFilter {
		val eventReceivers: ArrayList<JavaEventReceiver> = ArrayList()
		// we'll only look in the TeamCode module
		override val targets = TeamCodeSearch()

		override fun init() {
			eventReceivers.clear()
		}

		override fun filter(clazz: Class<*>) {
			eventReceivers.addAll(
				clazz.staticInstancesOf(
					JavaEventReceiver::class.java
				)
			)
		}
	}

}