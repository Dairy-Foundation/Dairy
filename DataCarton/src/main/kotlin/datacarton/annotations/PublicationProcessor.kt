package datacarton.annotations

import datacarton.CartonComponentRenderer
import org.firstinspires.ftc.robotcore.external.Telemetry.Line
import java.lang.reflect.Field
import java.util.function.Consumer

interface PublicationProcessor : Consumer<CartonComponentRenderer> {
	fun initPublication()
	val annotation: Class<out Annotation> // todo use this
	fun updatePublication()
}

sealed class ReflectionPublicationProcessor(private val output: Any, private val field: Field) : PublicationProcessor {
	private var outputBuilder = StringBuilder()

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

class TelemetryPublicationProcessor(telemetryLine: Line) : ReflectionPublicationProcessor(telemetryLine, Line::class.java.getDeclaredField("lineCaption")) {
	override val annotation: Class<out Annotation> = ExportToDriverStation::class.java
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
