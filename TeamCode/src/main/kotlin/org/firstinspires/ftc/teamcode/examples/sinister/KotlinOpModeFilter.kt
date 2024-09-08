package org.firstinspires.ftc.teamcode.examples.sinister

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.apphooks.OpModeRegistrar
import dev.frozenmilk.sinister.getAllAnnotations
import dev.frozenmilk.sinister.targeting.TeamCodeSearch
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMetaAndClass

@Suppress("unused")
object KotlinOpModeFilter : SinisterFilter, OpModeRegistrar {

	//
	// change to false to turn off all `@Debug` OpMode Registration
	private const val ENABLED = true
	//

	private val opModeMetaAndClasses = ArrayList<OpModeMetaAndClass>()

	// we only want to search the contents of the TeamCode module
	override val targets = TeamCodeSearch()

	override fun init() {
		opModeMetaAndClasses.clear()
	}

	override fun filter(clazz: Class<*>) {
		if (!ENABLED) return
		if (!OpMode::class.java.isAssignableFrom(clazz)) return
		val annotations = clazz.getAllAnnotations { it is Debug }
		if (annotations.isEmpty()) return
		val debug = annotations[0] as Debug
		val meta = OpModeMeta.Builder()
			.setFlavor(debug.flavour)
			.setName(debug.name)
			.setGroup(debug.group)
			.build()
		@Suppress("UNCHECKED_CAST")
		opModeMetaAndClasses.add(OpModeMetaAndClass(meta, clazz as Class<OpMode>))
	}

	override fun registerOpModes(opModeManager: AnnotatedOpModeManager) {
		opModeMetaAndClasses.forEach {
			opModeManager.register(it.meta, it.clazz)
		}
	}

	// realistically this should not
	@MustBeDocumented
	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	annotation class Debug(
		/**
		 * The type of OpMode that this should be registered as
		 */
		val flavour: OpModeMeta.Flavor = OpModeMeta.Flavor.TELEOP,
		/**
		 * The name to be used on the driver station display. If empty, the name of
		 * the OpMode class will be used.
		 * @return the name to use for the OpMode on the driver station
		 */
		val name: String = "",
		/**
		 * Optionally indicates a group of other OpModes with which the annotated
		 * OpMode should be sorted on the driver station OpMode list.
		 * @return the group into which the annotated OpMode is to be categorized
		 */
		val group: String = ""
	)
}