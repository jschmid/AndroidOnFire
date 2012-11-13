package pro.schmid.android.androidonfire;

import java.util.concurrent.atomic.AtomicInteger;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.Pushed;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;

import com.google.gson.JsonElement;

class FirebaseJavaScriptInterface {
	private static final String TAG = FirebaseJavaScriptInterface.class.getSimpleName();

	private final WebView mWebView;
	private final SparseArray<DataEvent> mListeners = new SparseArray<DataEvent>();
	private final AtomicInteger mMethodCounter = new AtomicInteger();

	protected FirebaseJavaScriptInterface(WebView webView) {
		this.mWebView = webView;
	}

	public void set(Firebase endpoint, JsonElement obj) {
		String method = "set('" + endpoint.toString() + "', " + obj.toString() + ")";
		loadMethod(method);
	}

	private String mPushName = null;

	public synchronized void push(Firebase endpoint, JsonElement obj, Pushed callback) {
		String method = "push('" + endpoint.toString() + "', " + obj.toString() + ")";
		loadMethod(method);

		// TODO semaphore ?
		try {
			wait();
		} catch (InterruptedException e) {
		}

		Log.d(TAG, "Finished " + mPushName);
		if (callback != null) {
			Firebase newBase = new Firebase(this, endpoint, mPushName);
			callback.pushed(newBase);
		}
	}

	public synchronized void pushed(String name) {
		Log.d(TAG, "Pushed " + name);
		mPushName = name;
		notify();
	}

	public void remove(Firebase endpoint) {
		String method = "remove('" + endpoint.toString() + "')";
		loadMethod(method);
	}

	public void on(String endpoint, EventType ev, DataEvent callback) {
		int methodId = mMethodCounter.incrementAndGet();
		this.mListeners.put(methodId, callback);
		loadMethod("on('" + endpoint + "', '" + ev + "', " + methodId + ")");
	}

	public void onEvent(String endpoint, int methodId, String name, String jsonString, String prevChildName) {
		DataEvent listener;
		if ((listener = this.mListeners.get(methodId)) != null) {
			Firebase parent = FirebaseEngine.getInstance().newFirebase(endpoint);
			DataSnapshot snapshot = new DataSnapshot(parent.child(name), jsonString);
			listener.callback(snapshot, prevChildName);
		}
	}

	private void loadMethod(String method) {
		Log.d(TAG, method);
		mWebView.loadUrl("javascript:" + method);
	}
}
