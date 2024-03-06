package org.firstinspires.ftc.teamcode.examples.sinister;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.ftccommon.external.OnCreate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import dev.frozenmilk.sinister.SinisterFilter;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.sinister.targeting.SearchTarget;
import dev.frozenmilk.sinister.targeting.WideSearch;

@SuppressWarnings("unused")
final class JavaOnCreateFilter implements SinisterFilter {
	private JavaOnCreateFilter() {}
	private static final JavaOnCreateFilter INSTANCE = new JavaOnCreateFilter();
	// Search everything except for the SDK's default search exclusions
	private static final SearchTarget SEARCH_TARGET = new WideSearch();
	@NonNull
	@Override
	public SearchTarget getTargets() {
		return SEARCH_TARGET;
	}
	
	@Override
	public void filter(@NonNull Class<?> clazz) {
		List<Method> methods = SinisterUtil.getAllMethods(clazz);
		methods.forEach(method -> {
			if (method.isAnnotationPresent(OnCreate.class) && SinisterUtil.isStatic(method)) {
				method.setAccessible(true);
				try {
					// yes a context should be passed here, but that's besides the point
					method.invoke(null);
				} catch (IllegalAccessException | InvocationTargetException e) {
					RobotLog.ww("@OnCreate Filter", "Error occurred while running @OnCreate method: " + Arrays.toString(e.getStackTrace()));
				}
			}
		});
	}
}
