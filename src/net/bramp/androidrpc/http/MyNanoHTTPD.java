package net.bramp.androidrpc.http;


import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import net.bramp.androidrpc.RPC;
import net.bramp.androidrpc.R;
import net.bramp.org.json.JSONException;
import net.bramp.org.json.JSONObject;
import net.bramp.org.json.JSONStringer;


import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * API:
 *   /sms/send?to=XXX-XXX-XXXX&text=?????
 *   
 *   /sms/list?older=NNNNN&direction=mt
 * 
 * 
 * TODO
 * 	add SMSC to send API
 *  add data SMS to send API
 * 
 * @author bramp
 *
 */
public final class MyNanoHTTPD extends NanoHTTPD {

	private static final boolean DEBUG = false;
	private static final String TAG = "MyNanoHTTPd";
	
	final RPC rpc;
	final Context context;

	public MyNanoHTTPD(Context context, int port) throws IOException {
		super(port, null);
		this.rpc = new RPC(context);
		this.context = context;
	}

	protected Response createErrorResponse(String status, String reason) {
		try {
			JSONObject json = new JSONObject();
			json.put("status", "error");
			json.put("reason", reason);

			return new Response( status, MIME_JSON, json.toString() );

		} catch (JSONException e) {
			// Uncheck this exception
			throw new RuntimeException(e);
		}
	}

	protected static Response createSuccessResponse() throws JSONException {
		JSONStringer json = new JSONStringer();

		json.object();
		return createSuccessResponse(json);
	}

	protected static Response createSuccessResponse(JSONStringer json) throws JSONException {
		json.key("status").value("ok");
		json.endObject();

		return new Response( HTTP_OK, MIME_JSON, json.toString() );
	}

	public Response serveSMSSend(String uri, String method, Properties header, Properties parms, Properties files) throws JSONException {
		String to   = parms.getProperty("to");
		String text = parms.getProperty("text");

		if (to == null || text == null || to.isEmpty() || text.isEmpty()) {
			return createErrorResponse(HTTP_OK, "both to and text parameters must be set");
		}

		rpc.sendSms(to, text);

		return createSuccessResponse();
	}

	public Response serveSMSList(String uri, String method, Properties header, Properties parms, Properties files) throws JSONException {

		String after = parms.getProperty("after");
		String direction = parms.getProperty("direction");

		long afterId = Long.MAX_VALUE;
		boolean mo = "mo".equalsIgnoreCase(direction);

		if (after != null) {
			try {
				afterId = Long.parseLong(after);

			} catch (NumberFormatException e) {
				return createErrorResponse(HTTP_OK, "after is an invalid id");
			}
		}

		JSONStringer json = new JSONStringer();
		json.object();

		rpc.listSms(json, afterId, mo);

		return createSuccessResponse(json);
	}

	public Response serveResource(String status, String mimeType, int resourceId) {
		Resources resources = context.getResources();
		return new Response(status, mimeType, resources.openRawResource(resourceId));
	}

	public void logRequestInfo(String uri, String method, Properties header, Properties parms, Properties files) {
		Log.i(TAG, method + " '" + uri + "' ");

		if (DEBUG) {
			Enumeration<?> e = header.propertyNames();
			while ( e.hasMoreElements()) {
				String value = (String)e.nextElement();
				Log.d(TAG, "  HDR: '" + value + "' = '" + header.getProperty( value ) + "'" );
			}
	
			e = parms.propertyNames();
			while ( e.hasMoreElements())
			{
				String value = (String)e.nextElement();
				Log.d(TAG, "  PRM: '" + value + "' = '" + parms.getProperty( value ) + "'" );
			}
	
			e = files.propertyNames();
			while ( e.hasMoreElements())
			{
				String value = (String)e.nextElement();
				Log.d(TAG, "  UPLOADED: '" + value + "' = '" + files.getProperty( value ) + "'" );
			}
		}
	}

	
	/**
	 * Override this to customize the server.<p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method	"GET", "POST" etc.
	 * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @param header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 * @throws JSONException 
	 */
	@Override
	public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {

		logRequestInfo(uri, method, header, parms, files);

		try {
			//
			// Serve Static files
			if (uri.equals("/") || uri.equals("/index.html")) {
				return serveResource(HTTP_OK, MIME_HTML, R.raw.sms);
			}

			if (uri.equals("/jquery_1_7_2_js.min.js")) {
				return serveResource(HTTP_OK, MIME_JAVASCRIPT, R.raw.jquery_1_7_2_js);
			}

			if (uri.equals("/bootstrap_js.min.js")) {
				return serveResource(HTTP_OK, MIME_JAVASCRIPT, R.raw.bootstrap_js);
			}

			if (uri.equals("/tmpl_js.js")) {
				return serveResource(HTTP_OK, MIME_JAVASCRIPT, R.raw.tmpl_js);
			}

			if (uri.equals("/bootstrap_css.min.css")) {
				return serveResource(HTTP_OK, MIME_CSS, R.raw.bootstrap_css);
			}
			
			if (uri.equals("/bootstrap_responsive_css.min.css")) {
				return serveResource(HTTP_OK, MIME_CSS, R.raw.bootstrap_responsive_css);
			}

			if (uri.equals("/glyphicons_halflings.png")) {
				return serveResource(HTTP_OK, MIME_PNG, R.raw.glyphicons_halflings);
			}

			//
			// Serve APIs
			if (uri.equals("/messages/list")) {
				return serveSMSList(uri, method, header, parms, files);
			}
	
			if (uri.equals("/messages/send")) {
				return serveSMSSend(uri, method, header, parms, files);
			}
	
			// 404
			return serveResource(HTTP_NOTFOUND, MIME_HTML, R.raw.error404);

		} catch (Throwable t) {
			Log.e(TAG, "serve", t);
			return createErrorResponse(HTTP_INTERNALERROR, t.toString() );
		}
	}

	public Response serveFile( String uri, Properties header, File homeDir, boolean allowDirectoryListing ) {
		// Disable the serving of the file system
		return null;
	}
	
	public final static String
		MIME_JSON = "application/json",
		MIME_JAVASCRIPT = "text/javascript",
		MIME_CSS = "text/css",
		MIME_PNG = "image/png";

}
