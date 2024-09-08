package org.firstinspires.ftc.teamcode.examples.sinister;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.frozenmilk.sinister.SinisterFilter;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.sinister.targeting.SearchTarget;
import dev.frozenmilk.sinister.targeting.TeamCodeSearch;

public final class JavaEventManager {
	// we'll make it possible to publish an event from anywhere
	public static void publishEvent(int eventLevel, String message) {
		EventReceiverFilter.eventReceivers.forEach(javaEventReceiver ->
				javaEventReceiver.receiveEvent(eventLevel, message)
		);
	}
	// this will collect the instances of EventReceiver
	private static final class EventReceiverFilter implements SinisterFilter {
		private EventReceiverFilter() {}
		public static final EventReceiverFilter INSTANCE = new EventReceiverFilter();
		private static final ArrayList<JavaEventReceiver> eventReceivers = new ArrayList<>();
		
		// we'll only look in the TeamCode module
		private static final SearchTarget searchTarget = new TeamCodeSearch();
		@NonNull
		@Override
		public SearchTarget getTargets() {
			return searchTarget;
		}
		
		@Override
		public void init() {
			eventReceivers.clear();
		}
		
		@Override
		public void filter(@NonNull Class<?> clazz) {
			eventReceivers.addAll(SinisterUtil.staticInstancesOf(clazz, JavaEventReceiver.class));
		}
	}
}
