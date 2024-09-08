package org.firstinspires.ftc.teamcode.examples.apphooks;

import android.content.Context;
import android.view.Menu;

import androidx.annotation.NonNull;

import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.robotcore.eventloop.opmode.AnnotatedOpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.util.WebHandlerManager;

import dev.frozenmilk.sinister.apphooks.OnCreate;
import dev.frozenmilk.sinister.apphooks.OnCreateEventLoop;
import dev.frozenmilk.sinister.apphooks.OnCreateMenu;
import dev.frozenmilk.sinister.apphooks.OnDestroy;
import dev.frozenmilk.sinister.apphooks.OpModeRegistrar;
import dev.frozenmilk.sinister.apphooks.WebHandlerRegistrar;

@SuppressWarnings("unused")
public class JavaOverview {
	private JavaOverview() {}
	// we could do whatever we want here!
	
	/**
	 * this is a private static inner class, to prevent AppHook code from leaking to the public api
	 */
	private static class AppHook implements OnCreate, OnCreateEventLoop, OnCreateMenu, OnDestroy, OpModeRegistrar, WebHandlerRegistrar {
		private AppHook() {}
		/**
		 * Sinister's scanning looks for instances to work with. In kotlin, this is an object class, in Java we need to re-create that by hand.
		 */
		private static final AppHook INSTANCE = new AppHook();
		
		@Override
		public void onCreate(@NonNull Context context) {
			// runs when the activity is created
		}
		
		@Override
		public void onCreateEventLoop(@NonNull Context context, @NonNull FtcEventLoop ftcEventLoop) {
			// runs when the event loop is created
			
			// this is how to get the OpModeManager
			OpModeManagerImpl opModeManager = ftcEventLoop.getOpModeManager();
		}
		
		@Override
		public void onCreateMenu(@NonNull Context context, @NonNull Menu menu) {
			// allows you to edit the menu on the control hub, not super useful
		}
		
		@Override
		public void onDestroy(@NonNull Context context) {
			// when the app shuts down
		}
		
		@Override
		public void registerOpModes(@NonNull AnnotatedOpModeManager opModeManager) {
			// allows you to register more OpModes yourself, combine this with a Sinister Filter for custom OpMode annotations and registration.
		}
		
		@Override
		public void webHandlerRegistrar(@NonNull Context context, @NonNull WebHandlerManager webHandlerManager) {
			// allows you to work with the web server
		}
	}
}
