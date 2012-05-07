package net.bramp.androidrpc;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.util.Log;

public abstract class NetUtils {

	private static final String TAG = "NetUtils";

	public static ArrayList<String> getLocalIpAddresses() {
		ArrayList<String> addresses = new ArrayList<String>();

	    try {
	    	Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
	        while( en.hasMoreElements() ) {
	            NetworkInterface intf = en.nextElement();
	            Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();

	            while ( enumIpAddr.hasMoreElements() ) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                	addresses.add( inetAddress.getHostAddress() );
	                }
	            }
	        }

	    } catch (SocketException e) {
	        Log.e(TAG, "Getting list of IP addresses", e);
	    }

	    return addresses;
	}
}
