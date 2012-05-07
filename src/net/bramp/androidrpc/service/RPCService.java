package net.bramp.androidrpc.service;

import java.io.IOException;

import net.bramp.androidrpc.R;
import net.bramp.androidrpc.activity.MainActivity;
import net.bramp.androidrpc.http.MyNanoHTTPD;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public final class RPCService extends Service {

	private static final String TAG = "RPCService";
	private static final String PREF = "RPCServicePreference";
	private static final String PREF_ENABLED = "serviceEnabled";

	private MyNanoHTTPD httpd;
	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private final int NOTIFICATION = R.string.service_started;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");

        try {
			httpd = new MyNanoHTTPD(this, 8080);
			httpd.start();

		} catch (IOException e) {
			showNotification(R.string.service_failed);
		}
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting. We put an icon in the
		// status bar.
		showNotification(R.string.service_started);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");

		httpd.stop();
		httpd = null;

		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);

		// Tell the user we stopped.
		Toast.makeText(getApplicationContext(), R.string.service_stopped, Toast.LENGTH_LONG).show();
	}


	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification(int textId) {
		final CharSequence text = getText(R.string.service_started);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		final Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

		notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);

		mNM.notify(NOTIFICATION, notification);
	}
	
	public static boolean isEnabled(Context context) {
        SharedPreferences settings = context.getSharedPreferences(RPCService.PREF, MODE_PRIVATE);
        return settings.getBoolean(RPCService.PREF_ENABLED, true);
	}
	
	public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences settings = context.getSharedPreferences(RPCService.PREF, MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean(RPCService.PREF_ENABLED, enabled);
		editor.apply();
		
	}
}
