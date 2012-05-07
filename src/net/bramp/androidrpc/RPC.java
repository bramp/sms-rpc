package net.bramp.androidrpc;

import net.bramp.androidrpc.service.RPCService;
import net.bramp.org.json.JSONException;
import net.bramp.org.json.JSONWriter;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

/**
 * Main object for conducting all the tasks
 * @author bramp
 *
 */
public final class RPC {

	public static final String SMS_INBOX = "content://sms/inbox";
	public static final String SMS_SENT  = "content://sms/sent";

	Context context;

	public RPC(Context context) {
		this.context = context;
	}

	public void sendSms(String to, String text) {
		SmsManager sms = SmsManager.getDefault();

		PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, RPCService.class), 0);

		sms.sendTextMessage(to, null, text, pi, null);
	}

	public void listSms(JSONWriter json, long id, boolean mo) throws JSONException {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(
			Uri.parse(mo ? SMS_SENT :  SMS_INBOX), 
			new String[] { "_id", "address", "date", "body" },
			"_id < ?",
			new String[] { Long.toString(id) },
			"_id DESC"
		);

		json.key("count").value(cursor.getCount());
		json.key("messages");

		json.array();

		int limit = 10;
		if ( cursor.moveToFirst() ) {
			do {
                json.object();

                json.key("id").value       ( cursor.getLong(0)   );
                json.key("address").value  ( cursor.getString(1) );
                json.key("timestamp").value( cursor.getLong(2)   );
                json.key("message").value  ( cursor.getString(3) );

                json.endObject();
                
				limit--;
			} while (cursor.moveToNext() && limit > 0);
		}
		
		json.endArray();
	}	
}
