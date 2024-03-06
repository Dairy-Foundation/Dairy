package org.firstinspires.ftc.teamcode.examples.sinister;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.Objects;

import dev.frozenmilk.sinister.SinisterFilter;
import dev.frozenmilk.sinister.targeting.SearchTarget;
import dev.frozenmilk.sinister.targeting.TeamCodeSearch;

@SuppressWarnings("unused")
final class JavaOpModeFilter implements SinisterFilter {
	private JavaOpModeFilter() {}
	private static final JavaOpModeFilter INSTANCE = new JavaOpModeFilter();
	// we only want to search the contents of the TeamCode module
	private static final SearchTarget SEARCH_TARGET = new TeamCodeSearch();
	@NonNull
	@Override
	public SearchTarget getTargets() {
		return SEARCH_TARGET;
	}
	
	@Override
	public void filter(@NonNull Class<?> clazz) {
		if (!clazz.isInstance(OpMode.class)) return;
		boolean auto = clazz.isAnnotationPresent(Autonomous.class);
		boolean teleop = clazz.isAnnotationPresent(TeleOp.class);
		if (!(auto || teleop)) return;
		if (auto && teleop) throw new IllegalStateException("OpMode has both @Autonomous and @TeleOp annotations, it may only have one.");
		String name = "";
		if (auto) name = Objects.requireNonNull(clazz.getAnnotation(Autonomous.class)).name();
		if (teleop) name = Objects.requireNonNull(clazz.getAnnotation(TeleOp.class)).name();
		RobotLog.vv("OpMode Filter", "Registered { " + clazz.getSimpleName() + " } as { " + name + " }" );
	}
}
