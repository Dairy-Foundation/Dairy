package org.firstinspires.ftc.teamcode.examples.sinister;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMetaAndClass;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import dev.frozenmilk.sinister.SinisterFilter;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.sinister.apphooks.OpModeRegistrar;
import dev.frozenmilk.sinister.targeting.SearchTarget;
import dev.frozenmilk.sinister.targeting.TeamCodeSearch;

@SuppressWarnings("unused")
public final class JavaOpModeFilter implements SinisterFilter, OpModeRegistrar {
	
	//
	// change to false to turn off all `@Debug` OpMode Registration
	private static final boolean ENABLED = true;
	//
	
	private JavaOpModeFilter() {}
	private static final JavaOpModeFilter INSTANCE = new JavaOpModeFilter();
	// we only want to search the contents of the TeamCode module
	private static final SearchTarget SEARCH_TARGET = new TeamCodeSearch();
	@NonNull
	@Override
	public SearchTarget getTargets() {
		return SEARCH_TARGET;
	}
	
	private final ArrayList<OpModeMetaAndClass> opModeMetaAndClasses = new ArrayList<>();
	
	@Override
	public void init() {
		opModeMetaAndClasses.clear();
	}
	
	@Override
	public void filter(@NonNull Class<?> clazz) {
		if (!ENABLED) return;
		if (!OpMode.class.isAssignableFrom(clazz)) return;
		List<Annotation> annotations = SinisterUtil.getAllAnnotations(clazz, (annotation -> annotation instanceof Debug));
		if (annotations.isEmpty()) return;
		Debug debug = (Debug) annotations.get(0);
		OpModeMeta meta = new OpModeMeta.Builder()
				.setFlavor(debug.flavour())
				.setName(debug.name())
				.setGroup(debug.group())
				.build();
		//noinspection unchecked
		opModeMetaAndClasses.add(new OpModeMetaAndClass(meta, (Class<OpMode>) clazz));
	}
	
	@Override
	public void registerOpModes(@NonNull AnnotatedOpModeManager opModeManager) {
		for (OpModeMetaAndClass opModeMetaAndClass : opModeMetaAndClasses) {
			opModeManager.register(opModeMetaAndClass.meta, opModeMetaAndClass.clazz);
		}
	}
	
	@Documented
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Debug {
		/**
		 * The type of OpMode that this should be registered as
		 */
		OpModeMeta.Flavor flavour() default OpModeMeta.Flavor.TELEOP;
		/**
		 * The name to be used on the driver station display. If empty, the name of
		 * the OpMode class will be used.
		 * @return the name to use for the OpMode on the driver station
		 */
		String name() default "";
		
		/**
		 * Optionally indicates a group of other OpModes with which the annotated
		 * OpMode should be sorted on the driver station OpMode list.
		 * @return the group into which the annotated OpMode is to be categorized
		 */
		String group() default "";
	}
}
