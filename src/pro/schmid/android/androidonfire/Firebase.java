package pro.schmid.android.androidonfire;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.Transaction;
import pro.schmid.android.androidonfire.callbacks.TransactionComplete;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Firebase {
	private final FirebaseJavaScriptInterface mJsInterface;
	private final Firebase mParent;
	private final String mEndpoint;
	private final String mName;

	protected Firebase(FirebaseJavaScriptInterface jsInterface, String name) {
		this(jsInterface, null, name);
	}

	protected Firebase(FirebaseJavaScriptInterface jsInterface, Firebase parent, String name) {
		this.mParent = parent;
		this.mJsInterface = jsInterface;
		this.mName = name;

		if (mParent != null) {
			mEndpoint = mParent.toString() + "/" + mName;
		} else {
			mEndpoint = mName;
		}
	}

	// TODO child path
	public Firebase child(String childPath) {
		return new Firebase(this.mJsInterface, this, childPath);
	}

	public Firebase parent() {
		return this.mParent;
	}

	public String name() {
		return mName;
	}

	@Override
	public String toString() {
		return mEndpoint;
	}

	// TODO what does set(null) do ?
	public void set(JsonElement obj) {
		this.mJsInterface.set(mEndpoint, obj);
	}

	// TODO on complete
	public void remove() {
		this.mJsInterface.remove(mEndpoint);
	}

	public synchronized Firebase push() {
		return this.push(new JsonObject());
	}

	public synchronized Firebase push(JsonElement obj) {
		return this.mJsInterface.push(this, obj);
	}

	// TODO push(obj, onComplete)

	public void transaction(Transaction transaction) {
		transaction(transaction, null);
	}

	public void transaction(Transaction transaction, TransactionComplete onComplete) {
		this.mJsInterface.transaction(mEndpoint, transaction, onComplete);
	}

	public void setOnDisconnect(JsonElement obj) {
		this.mJsInterface.setOnDisconnect(mEndpoint, obj);
	}

	public void removeOnDisconnect() {
		this.mJsInterface.removeOnDisconnect(mEndpoint);
	}

	public DataEvent on(EventType ev, DataEvent callback) {
		this.mJsInterface.on(mEndpoint, ev, callback);
		return callback;
	}

	public void off() {
		this.mJsInterface.off(mEndpoint);
	}

	public void off(EventType ev) {
		this.mJsInterface.off(mEndpoint, ev);
	}

	public void off(EventType ev, DataEvent callback) {
		this.mJsInterface.off(mEndpoint, ev, callback);
	}

	public void once(EventType ev, DataEvent callback) {
		this.mJsInterface.once(mEndpoint, ev, callback);
	}
}
