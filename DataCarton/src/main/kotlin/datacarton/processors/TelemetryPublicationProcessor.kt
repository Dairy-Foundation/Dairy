package datacarton.processors

import com.qualcomm.robotcore.util.ElapsedTime
import datacarton.CartonComponentRenderer
import dev.frozenmilk.util.cell.MirroredCell
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl
import java.lang.reflect.Field

sealed class ReflectionPublicationProcessor(private val output: Any, private val field: Field) : PublicationProcessor {
	private val outputBuilder = StringBuilder()
	init {
		field.isAccessible = true
	}

	override fun initPublication() {
		outputBuilder.clear()
	}

	override fun accept(p0: CartonComponentRenderer) {
		outputBuilder.append(p0).append("\n")
	}

	override fun updatePublication() {
		this.field.set(output, outputBuilder.toString())
	}
}

class TelemetryPublicationProcessor(private val telemetry: Telemetry) : ReflectionPublicationProcessor(telemetry.addLine(), Telemetry.Line::class.java.getDeclaredField("lineCaption")) {
	init {
		telemetry.isAutoClear = false
		telemetry.clearAll()
		telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE)
		telemetry.captionValueSeparator = ""
		telemetry.itemSeparator = ""
	}
	private val telemetryTimer = MirroredCell<ElapsedTime>(telemetry as TelemetryImpl, "transmissionTimer").get()
	override fun ignoreUpdate(): Boolean {
		return !(telemetryTimer.milliseconds() > telemetry.msTransmissionInterval)
	}
}