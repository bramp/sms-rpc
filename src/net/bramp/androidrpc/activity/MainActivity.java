package net.bramp.androidrpc.activity;

import net.bramp.androidrpc.NetUtils;
import net.bramp.androidrpc.R;
import net.bramp.androidrpc.service.RPCService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.ToggleButton;

public final class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "MainActivity";

	ToggleButton toggle;
	WebView webview;

	public boolean isMyServiceRunning() {
		String myService = RPCService.class.getName();
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (myService.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        boolean enabled = RPCService.isEnabled(this);

        toggle = (ToggleButton) findViewById(R.id.toggleService);
        toggle.setOnClickListener(this);        
        toggle.setChecked( enabled );

        webview = (WebView) findViewById(R.id.webview);
        
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);

        if (enabled) {
        	if (!isMyServiceRunning()) {
        		startRPCService();
        	} else {
        		webview.loadUrl("http://localhost:8080/");
        	}
        }

    	TextView text = (TextView) findViewById(R.id.text);
        text.setText("Listening on: " + NetUtils.getLocalIpAddresses() + " port 8080" );
    }

    protected void startRPCService() {
		Log.i(TAG, "Starting RPCService");

		startService(new Intent(this, RPCService.class));

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				webview.loadUrl("http://localhost:8080/");
			}

		}, 1000);
    }
    
    protected void stopRPCService() {
		Log.i(TAG, "Stopping RPCService");
		stopService(new Intent(this, RPCService.class));
    }

	@Override
	public void onClick(View view) {
		if (toggle == view) {
			if (toggle.isChecked()) { // Toggle service
				startRPCService();
				RPCService.setEnabled(this, true);

			} else {
				stopRPCService();
				RPCService.setEnabled(this, false);
			}
		}
	}
}