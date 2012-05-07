package net.bramp.androidrpc.receiver;

import net.bramp.androidrpc.service.RPCService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Simple Receiver to start up our Service on bootup
 * 
 * @author bramp
 *
 */
public final class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

        boolean enabled = RPCService.isEnabled(context);

        if (!enabled)
        	return;
		
		try {
			Log.i(TAG, "Starting RPCService");
			context.startService(new Intent(context, RPCService.class));

		} catch (Exception e) {
			Log.e(TAG, "onReceive", e);
		}
	}
}