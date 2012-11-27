package pro.schmid.android.androidonfire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.Transaction;
import pro.schmid.android.androidonfire.callbacks.TransactionComplete;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

class FirebaseJavaScriptInterface {
	private static final String TAG = FirebaseJavaScriptInterface.class.getSimpleName();

	private final WebView mWebView;
	private final SparseArray<DataEvent> mListenersIds = new SparseArray<DataEvent>();
	private final SparseArray<Transaction> mTransactions = new SparseArray<Transaction>();
	private final SparseArray<TransactionComplete> mTransactionsComplete = new SparseArray<TransactionComplete>();

	// Base URL => Event type => Callbacks => method id list
	private final ConcurrentHashMap<String, Map<EventType, Map<DataEvent, List<Integer>>>> mListenersMap = new ConcurrentHashMap<String, Map<EventType, Map<DataEvent, List<Integer>>>>();
	private final AtomicInteger mMethodCounter = new AtomicInteger();

	protected FirebaseJavaScriptInterface(WebView webView) {
		this.mWebView = webView;
	}

	public void set(String endpoint, JsonElement obj) {
		String method = "set('" + endpoint + "', " + obj.toString() + ")";
		loadMethod(method);
	}

	private String mPushName = null;
	private final Semaphore mSemaphore = new Semaphore(0, true);

	public synchronized Firebase push(Firebase endpoint, JsonElement obj) {
		String method = "push('" + endpoint.toString() + "', " + obj.toString() + ")";
		loadMethod(method);

		try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
			return null;
		}

		Firebase newBase = new Firebase(this, endpoint, mPushName);
		return newBase;
	}

	/**
	 * Called by JS
	 */
	public void pushed(String name) {
		mPushName = name;
		mSemaphore.release();
	}

	public void transaction(String endpoint, Transaction transaction, TransactionComplete onComplete) {
		int methodId = mMethodCounter.incrementAndGet();
		mTransactions.put(methodId, transaction);
		mTransactionsComplete.put(methodId, onComplete);

		String method = "transaction('" + endpoint + "', " + methodId + ")";
		loadMethod(method);
	}

	/**
	 * Called by JS
	 */
	public String callTransactionMethod(String endpoint, int methodId, String json) {
		Transaction transaction = mTransactions.get(methodId);

		if (transaction == null) {
			return JsonNull.INSTANCE.toString();
		}

		JsonElement obj = FirebaseJsonParser.getInstance().parse(json);

		JsonElement ret = transaction.transaction(obj);

		if (ret != null) {
			return ret.toString();
		} else {
			return null;
		}
	}

	/**
	 * Called by JS
	 */
	public void transactionComplete(String endpoint, int methodId, boolean success, String name, String val, String reason) {

		Firebase parent = FirebaseEngine.getInstance().newFirebase(endpoint);
		DataSnapshot snapshot = new DataSnapshot(parent.child(name), val);

		TransactionComplete transactionComplete = mTransactionsComplete.get(methodId);

		if (transactionComplete != null) {
			transactionComplete.onComplete(success, snapshot, reason);
		}

		mTransactions.remove(methodId);
		mTransactionsComplete.remove(methodId);
	}

	public void remove(String endpoint) {
		String method = "remove('" + endpoint + "')";
		loadMethod(method);
	}

	public void setOnDisconnect(String endpoint, JsonElement obj) {
		String method = "setOnDisconnect('" + endpoint + "', " + obj.toString() + ")";
		loadMethod(method);
	}

	public void removeOnDisconnect(String endpoint) {
		String method = "removeOnDisconnect('" + endpoint + "')";
		loadMethod(method);
	}

	public void on(String endpoint, EventType ev, DataEvent callback) {
		int methodId = mMethodCounter.incrementAndGet();
		this.mListenersIds.put(methodId, callback);
		putCallBack(endpoint, ev, callback, methodId);

		String method = "on('" + endpoint + "', '" + ev + "', " + methodId + ")";
		loadMethod(method);
	}

	private void putCallBack(String endpoint, EventType ev, DataEvent callback, int methodId) {
		// Endpoint => event type
		Map<EventType, Map<DataEvent, List<Integer>>> child = this.mListenersMap.get(endpoint);
		if (child == null) {
			child = new ConcurrentHashMap<EventType, Map<DataEvent, List<Integer>>>();
			this.mListenersMap.put(endpoint, child);
		}

		// event type => callbacks
		Map<DataEvent, List<Integer>> callbacks = child.get(ev);
		if (callbacks == null) {
			callbacks = new ConcurrentHashMap<DataEvent, List<Integer>>();
			child.put(ev, callbacks);
		}

		// Callback => method ids list
		List<Integer> list = callbacks.get(callback);
		if (list == null) {
			list = new ArrayList<Integer>();
			callbacks.put(callback, list);
		}

		list.add(methodId);
	}

	/**
	 * Called by JS
	 */
	public void onEvent(String endpoint, int methodId, String name, String val, String prevChildName) {
		DataEvent listener;
		if ((listener = this.mListenersIds.get(methodId)) != null) {
			Firebase parent = FirebaseEngine.getInstance().newFirebase(endpoint);
			DataSnapshot snapshot = new DataSnapshot(parent.child(name), val);
			listener.callback(snapshot, prevChildName);
		}
	}

	public void off(String endpoint) {
		removeMethods(endpoint);

		String method = "off('" + endpoint.toString() + "')";
		loadMethod(method);
	}

	public void off(String endpoint, EventType ev) {
		removeMethods(endpoint, ev);

		String method = "off('" + endpoint.toString() + "', '" + ev + "')";
		loadMethod(method);
	}

	public void off(String endpoint, EventType ev, DataEvent callback) {
		Integer methodId = removeMethod(endpoint, ev, callback);

		if (methodId == null) {
			return;
		}

		String method = "off('" + endpoint.toString() + "', '" + ev + "', " + methodId + ")";
		loadMethod(method);
	}

	private void removeMethods(String endpoint) {
		Map<EventType, Map<DataEvent, List<Integer>>> parent = this.mListenersMap.remove(endpoint);
		if (parent == null) {
			return;
		}

		for (Map<DataEvent, List<Integer>> map : parent.values()) {
			for (List<Integer> item : map.values()) {
				for (Integer integer : item) {
					this.mListenersIds.remove(integer);
				}
			}
		}
	}

	private void removeMethods(String endpoint, EventType ev) {
		Map<EventType, Map<DataEvent, List<Integer>>> child = this.mListenersMap.get(endpoint);
		if (child == null) {
			return;
		}

		Map<DataEvent, List<Integer>> map = child.remove(ev);
		for (List<Integer> item : map.values()) {
			for (Integer integer : item) {
				this.mListenersIds.remove(integer);
			}
		}
	}

	private Integer removeMethod(String endpoint, EventType ev, DataEvent callback) {
		Map<EventType, Map<DataEvent, List<Integer>>> child = this.mListenersMap.get(endpoint);
		if (child == null) {
			return null;
		}

		Map<DataEvent, List<Integer>> callbacks = child.get(ev);
		if (callbacks == null) {
			return null;
		}

		List<Integer> list = callbacks.get(callback);
		if (list == null) {
			return null;
		}

		if (list.size() <= 0) {
			return null;
		}

		Integer id = list.remove(0);
		if (id != null) {
			this.mListenersIds.remove(id);
		}
		return id;
	}

	public void once(final String endpoint, final EventType ev, final DataEvent callback) {
		this.on(endpoint, ev, new DataEvent() {
			@Override
			public void callback(DataSnapshot snapshot, String prevChildName) {
				off(endpoint, ev, this);
				callback.callback(snapshot, prevChildName);
			}
		});
	}

	private void loadMethod(String method) {
		Log.d(TAG, method);
		mWebView.loadUrl("javascript:" + method);
	}
}
