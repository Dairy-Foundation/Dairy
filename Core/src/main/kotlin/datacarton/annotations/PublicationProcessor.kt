package datacarton.annotations

import datacarton.CartonComponentRenderer
import java.lang.reflect.Field
import java.util.function.Consumer

interface PublicationProcessor : Consumer<CartonComponentRenderer> {
	fun initPublication()
	val annotation: Class<out Annotation>
	fun updatePublication()
}

class DriverStationPublicationProcessor(private val output: Any, private val field: Field) : PublicationProcessor {
	override val annotation: Class<out Annotation> = ExportToDriverStation::class.java
	private var outputBuilder = StringBuilder()

//	constructor() TODO("telemetry")

	init {
		field.isAccessible = true
	}

	override fun initPublication() {
		outputBuilder.clear()
	}

	override fun accept(p0: CartonComponentRenderer) {
		outputBuilder.append(p0)
	}

	override fun updatePublication() {
		this.field.set(output, outputBuilder.toString())
	}
}

class LogPublicationProcessor() : PublicationProcessor {
	override fun initPublication() {
		TODO("Not yet implemented")
	}

	override val annotation: Class<out Annotation> = ExportToLog::class.java
	override fun updatePublication() {
		TODO("Not yet implemented")
	}

	override fun accept(p0: CartonComponentRenderer) {
		TODO("Not yet implemented")
	}
}
