package pro.schmid.android.androidonfire;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.SynchonizedToServer;
import pro.schmid.android.androidonfire.callbacks.Transaction;
import pro.schmid.android.androidonfire.callbacks.TransactionComplete;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Public class mirroring the Firebase.js class.
 * Every method can be used almost exactly like its Javascript parent.
 */
public class Firebase {
	private final FirebaseJavaScriptInterface mJsInterface;
	private final String mEndpoint;
	private final String mName;
	private final String mParent;

	protected Firebase(FirebaseJavaScriptInterface jsInterface, String endpoint) {
		if (!endpoint.startsWith("https://") && !endpoint.startsWith("http://")) {
			throw new IllegalArgumentException("Endpoint must begin with http:// or https://");
		}

		this.mJsInterface = jsInterface;
		this.mEndpoint = endpoint;

		int doubleSlashes = endpoint.indexOf("//") + 2;
		int lastSlash = endpoint.lastIndexOf('/');

		// No slash, we are at the root
		if (lastSlash < doubleSlashes) {
			int dot = endpoint.indexOf('.', doubleSlashes);
			this.mName = endpoint.substring(doubleSlashes, dot);
			this.mParent = null;

			// Take the last string after the last slash, it will be the Firebase name
		} else {
			this.mName = endpoint.substring(lastSlash + 1);
			this.mParent = endpoint.substring(0, lastSlash);
		}
	}

	public Firebase child(String childPath) {
		int stringBegin = childPath.startsWith("/") ? 1 : 0;
		int stringEnd = childPath.endsWith("/") ? childPath.length() - 1 : childPath.length();
		childPath = childPath.substring(stringBegin, stringEnd);

		String endpoint = this.mEndpoint + "/" + childPath;

		return new Firebase(this.mJsInterface, endpoint);
	}

	public Firebase parent() {
		return new Firebase(this.mJsInterface, this.mParent);
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
		this.set(obj, null);
	}

	public void set(JsonElement obj, SynchonizedToServer onComplete) {
		this.mJsInterface.set(mEndpoint, obj, onComplete);
	}

	public void update(JsonElement obj) {
		this.update(obj, null);
	}

	public void update(JsonElement obj, SynchonizedToServer onComplete) {
		this.mJsInterface.update(mEndpoint, obj, onComplete);
	}

	public void remove() {
		this.remove(null);
	}

	public void remove(SynchonizedToServer onComplete) {
		this.mJsInterface.remove(mEndpoint, onComplete);
	}

	public synchronized Firebase push() {
		return this.push(new JsonObject());
	}

	public synchronized Firebase push(JsonElement obj) {
		return this.push(obj, null);
	}

	public synchronized Firebase push(JsonElement obj, SynchonizedToServer onComplete) {
		return this.mJsInterface.push(this, obj, onComplete);
	}

	public void setWithPriority(JsonElement obj, String priority) {
		this.setWithPriority(obj, priority, null);
	}

	public void setWithPriority(JsonElement obj, String priority, SynchonizedToServer onComplete) {
		this.mJsInterface.setWithPriority(mEndpoint, obj, priority, onComplete);
	}

	public void setPriority(String priority) {
		this.setPriority(priority, null);
	}

	public void setPriority(String priority, SynchonizedToServer onComplete) {
		this.mJsInterface.setPriority(mEndpoint, priority, onComplete);
	}

	/**
	 * Perform a transaction on the Firebase.
	 * 
	 * @param transaction
	 */
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

	public Query limit(int limit) {
		return new Query(mJsInterface, this).limit(limit);
	}

	public Query startAt() {
		return startAt(null, null);
	}

	public Query startAt(String priority) {
		return startAt(priority, null);
	}

	public Query startAt(String priority, String name) {
		return new Query(mJsInterface, this).startAt(priority, name);
	}

	public Query endAt() {
		return endAt(null, null);
	}

	public Query endAt(String priority) {
		return endAt(priority, null);
	}

	public Query endAt(String priority, String name) {
		return new Query(mJsInterface, this).endAt(priority, name);
	}
}
