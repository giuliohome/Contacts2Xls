package com.giulio.contentprovider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

/**
 * From Android 15 (targetSdk 35+) apps are drawn edge-to-edge: without this, the screen content
 * sits under the action bar, the navigation buttons and the keyboard.
 */
final class SystemBars {

	private SystemBars() {}

	static void applyInsets(Activity activity) {
		// Older versions already keep the content clear of the system bars.
		if (Build.VERSION.SDK_INT < 35) return;

		View content = activity.findViewById(android.R.id.content);
		content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
			@Override
			@TargetApi(35) // only installed on Android 15+, see the check above
			public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
				Insets bars = insets.getInsets(WindowInsets.Type.systemBars()
						| WindowInsets.Type.displayCutout()
						| WindowInsets.Type.ime());
				v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
				return insets;
			}
		});
		content.requestApplyInsets();
	}
}
