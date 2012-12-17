package pro.schmid.android.androidonfire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.SynchonizedToServer;
import pro.schmid.android.androidonfire.callbacks.Transaction;
import pro.schmid.android.androidonfire.callbacks.TransactionComplete;
import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.WebView;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/**
 * Class used internally to access the {@link WebView}.
 */
class FirebaseJavaScriptInterface {
	private static final String TAG = FirebaseJavaScriptInterface.class.getSimpleName();

	private final WebView mWebView;
	private final Activity mActivity;

	private final SparseArray<DataEvent> mListenersIds = new SparseArray<DataEvent>();
	private final SparseArray<SynchonizedToServer> mSynchronizedToServer = new SparseArray<SynchonizedToServer>();
	private final SparseArray<Transaction> mTransactions = new SparseArray<Transaction>();
	private final SparseArray<TransactionComplete> mTransactionsComplete = new SparseArray<TransactionComplete>();

	// Endpoint => Event type => Callbacks => method id list
	private final ConcurrentHashMap<String, Map<EventType, Map<DataEvent, List<Integer>>>> mListenersMap = new ConcurrentHashMap<String, Map<EventType, Map<DataEvent, List<Integer>>>>();

	// Endpoint => Query => Event type => Callbacks => method id list
	private final ConcurrentHashMap<String, Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>>> mQueryListenersMap = new ConcurrentHashMap<String, Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>>>();

	// Counter for the methods
	private final AtomicInteger mMethodCounter = new AtomicInteger();

	protected FirebaseJavaScriptInterface(WebView webView, Activity activity) {
		this.mWebView = webView;
		this.mActivity = activity;
	}

	protected void set(String endpoint, JsonElement obj, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "set('" + endpoint + "', " + obj.toString() + ", " + methodId + ")";
		} else {
			method = "set('" + endpoint + "', " + obj.toString() + ")";
		}
		loadMethod(method);
	}

	protected void update(String endpoint, JsonElement obj, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "update('" + endpoint + "', " + obj.toString() + ", " + methodId + ")";
		} else {
			method = "update('" + endpoint + "', " + obj.toString() + ")";
		}

		loadMethod(method);
	}

	protected void remove(String endpoint, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "remove('" + endpoint + "', " + methodId + ")";
		} else {
			method = "remove('" + endpoint + "')";
		}

		loadMethod(method);
	}

	private String mPushName = null;
	private final Semaphore mSemaphore = new Semaphore(0, true);

	protected synchronized Firebase push(Firebase endpoint, JsonElement obj, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "push('" + endpoint.toString() + "', " + obj.toString() + ", " + methodId + ")";
		} else {
			method = "push('" + endpoint.toString() + "', " + obj.toString() + ")";
		}
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

	protected void setWithPriority(String endpoint, JsonElement obj, String priority, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "setWithPriority('" + endpoint + "', " + obj.toString() + ", '" + priority + "', " + methodId + ")";
		} else {
			method = "setWithPriority('" + endpoint + "', " + obj.toString() + ", '" + priority + "')";
		}
		loadMethod(method);
	}

	protected void setPriority(String endpoint, String priority, SynchonizedToServer onComplete) {
		String method = null;
		if (onComplete != null) {
			int methodId = mMethodCounter.incrementAndGet();
			mSynchronizedToServer.put(methodId, onComplete);
			method = "setPriority('" + endpoint + "', '" + priority + "', " + methodId + ")";
		} else {
			method = "setPriority('" + endpoint + "', '" + priority + "')";
		}
		loadMethod(method);
	}

	protected void transaction(String endpoint, Transaction transaction, TransactionComplete onComplete) {
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

	protected void setOnDisconnect(String endpoint, JsonElement obj) {
		String method = "setOnDisconnect('" + endpoint + "', " + obj.toString() + ")";
		loadMethod(method);
	}

	protected void removeOnDisconnect(String endpoint) {
		String method = "removeOnDisconnect('" + endpoint + "')";
		loadMethod(method);
	}

	protected void on(String endpoint, EventType ev, DataEvent callback) {
		int methodId = mMethodCounter.incrementAndGet();
		this.mListenersIds.put(methodId, callback);
		putCallBack(endpoint, ev, callback, methodId);

		String method = "onFirebase('" + endpoint + "', '" + ev + "', " + methodId + ")";
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

	protected void off(String endpoint) {
		removeMethods(endpoint);

		String method = "offFirebase('" + endpoint.toString() + "')";
		loadMethod(method);
	}

	protected void off(String endpoint, EventType ev) {
		removeMethods(endpoint, ev);

		String method = "offFirebase('" + endpoint.toString() + "', '" + ev + "')";
		loadMethod(method);
	}

	protected void off(String endpoint, EventType ev, DataEvent callback) {
		Integer methodId = removeMethod(endpoint, ev, callback);

		if (methodId == null) {
			return;
		}

		String method = "offFirebase('" + endpoint.toString() + "', '" + ev + "', " + methodId + ")";
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

	protected void once(final String endpoint, final EventType ev, final DataEvent callback) {
		this.on(endpoint, ev, new DataEvent() {
			@Override
			public void callback(DataSnapshot snapshot, String prevChildName) {
				off(endpoint, ev, this);
				callback.callback(snapshot, prevChildName);
			}
		});
	}

	protected void onQuery(String endpoint, Query query, EventType ev, DataEvent callback) {

		int methodId = mMethodCounter.incrementAndGet();
		this.mListenersIds.put(methodId, callback);
		putCallBack(endpoint, query, ev, callback, methodId);

		StringBuilder sb = new StringBuilder();
		sb.append("onQuery('" + endpoint + "', '" + ev + "', " + methodId);
		addQueryToMethod(query, sb);
		sb.append(")");

		String method = sb.toString();
		loadMethod(method);
	}

	private void putCallBack(String endpoint, Query query, EventType ev, DataEvent callback, int methodId) {
		// Endpoint => Query
		Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>> endpointChild = this.mQueryListenersMap.get(endpoint);
		if (endpointChild == null) {
			endpointChild = new ConcurrentHashMap<Query, Map<EventType, Map<DataEvent, List<Integer>>>>();
			this.mQueryListenersMap.put(endpoint, endpointChild);
		}

		// Query => event type
		Map<EventType, Map<DataEvent, List<Integer>>> queryChild = endpointChild.get(query);
		if (queryChild == null) {
			queryChild = new ConcurrentHashMap<EventType, Map<DataEvent, List<Integer>>>();
			endpointChild.put(query, queryChild);
		}

		// event type => callbacks
		Map<DataEvent, List<Integer>> callbacks = queryChild.get(ev);
		if (callbacks == null) {
			callbacks = new ConcurrentHashMap<DataEvent, List<Integer>>();
			queryChild.put(ev, callbacks);
		}

		// Callback => method ids list
		List<Integer> list = callbacks.get(callback);
		if (list == null) {
			list = new ArrayList<Integer>();
			callbacks.put(callback, list);
		}

		list.add(methodId);
	}

	protected void offQuery(String endpoint, Query query) {

		removeMethods(endpoint, query);

		StringBuilder sb = new StringBuilder();
		sb.append("offQuery('" + endpoint + "', undefined, undefined");
		addQueryToMethod(query, sb);
		sb.append(")");

		String method = sb.toString();
		loadMethod(method);

	}

	protected void offQuery(String endpoint, Query query, EventType ev) {

		removeMethods(endpoint, query, ev);

		StringBuilder sb = new StringBuilder();
		sb.append("offQuery('" + endpoint + "', '" + ev + "', undefined");
		addQueryToMethod(query, sb);
		sb.append(")");

		String method = sb.toString();
		loadMethod(method);
	}

	protected void offQuery(String endpoint, Query query, EventType ev, DataEvent callback) {

		Integer methodId = removeMethod(endpoint, query, ev, callback);

		if (methodId == null) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("offQuery('" + endpoint + "', '" + ev + "', " + methodId);
		addQueryToMethod(query, sb);
		sb.append(")");

		String method = sb.toString();
		loadMethod(method);
	}

	private void removeMethods(String endpoint, Query query) {
		Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>> parent = this.mQueryListenersMap.remove(endpoint);
		if (parent == null) {
			return;
		}

		Map<EventType, Map<DataEvent, List<Integer>>> child = parent.get(query);
		if (child == null) {
			return;
		}

		for (Map<DataEvent, List<Integer>> map : child.values()) {
			for (List<Integer> item : map.values()) {
				for (Integer integer : item) {
					this.mListenersIds.remove(integer);
				}
			}
		}
	}

	private void removeMethods(String endpoint, Query query, EventType ev) {
		Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>> parent = this.mQueryListenersMap.get(endpoint);
		if (parent == null) {
			return;
		}

		Map<EventType, Map<DataEvent, List<Integer>>> child = parent.get(query);
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

	private Integer removeMethod(String endpoint, Query query, EventType ev, DataEvent callback) {
		Map<Query, Map<EventType, Map<DataEvent, List<Integer>>>> parent = this.mQueryListenersMap.get(endpoint);
		if (parent == null) {
			return null;
		}

		Map<EventType, Map<DataEvent, List<Integer>>> child = parent.get(query);
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

	protected void onceQuery(final String endpoint, final Query query, final EventType ev, final DataEvent callback) {
		this.onQuery(endpoint, query, ev, new DataEvent() {
			@Override
			public void callback(DataSnapshot snapshot, String prevChildName) {
				offQuery(endpoint, query, ev, this);
				callback.callback(snapshot, prevChildName);
			}
		});
	}

	private void addQueryToMethod(Query query, StringBuilder sb) {
		if (query.mLimit > -1) {
			sb.append(", " + query.mLimit);
		} else {
			sb.append(", undefined");
		}

		sb.append(", " + query.mStartSpecified.toString());

		if (query.mStartPriority != null) {
			sb.append(", '" + query.mStartPriority + "'");
		} else {
			sb.append(", undefined");
		}

		if (query.mStartName != null) {
			sb.append(", '" + query.mStartName + "'");
		} else {
			sb.append(", undefined");
		}

		sb.append(", " + query.mEndSpecified.toString());

		if (query.mEndPriority != null) {
			sb.append(", '" + query.mEndPriority + "'");
		} else {
			sb.append(", undefined");
		}

		if (query.mEndName != null) {
			sb.append(", '" + query.mEndName + "'");
		} else {
			sb.append(", undefined");
		}
	}

	/**
	 * Called by JS
	 */
	public void onEvent(String endpoint, int methodId, String name, String val, String priority, String prevChildName) {
		DataEvent listener;
		if ((listener = this.mListenersIds.get(methodId)) != null) {
			Firebase parent = FirebaseEngine.getInstance().newFirebase(endpoint);
			DataSnapshot snapshot = new DataSnapshot(parent.child(name), val, priority);
			listener.callback(snapshot, prevChildName);
		}
	}

	/**
	 * Called by JS
	 */
	public void synchronizedToServer(int methodId, boolean success) {
		SynchonizedToServer pushComplete = mSynchronizedToServer.get(methodId);
		if (pushComplete != null) {
			pushComplete.onComplete(success);
			mSynchronizedToServer.remove(methodId);
		}
	}

	private void loadMethod(String method) {
		Log.d(TAG, method);
		final String loaMethod = "javascript:" + method;

		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			mWebView.loadUrl(loaMethod);
		} else {
			this.mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mWebView.loadUrl(loaMethod);
				}
			});
		}
	}
}
