package pro.schmid.android.androidonfire;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;
import pro.schmid.android.androidonfire.callbacks.Pushed;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Firebase {
	private final FirebaseJavaScriptInterface mJsInterface;
	private final Firebase mParent;
	private final String mName;
	private final String mToString;

	protected Firebase(FirebaseJavaScriptInterface jsInterface, String name) {
		this(jsInterface, null, name);
	}

	protected Firebase(FirebaseJavaScriptInterface jsInterface, Firebase parent, String name) {
		this.mParent = parent;
		this.mJsInterface = jsInterface;
		this.mName = name;

		if (mParent != null) {
			mToString = mParent.toString() + "/" + mName;
		} else {
			mToString = mName;
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
		return mToString;
	}

	// TODO what does set(null) do ?
	public void set(JsonElement obj) {
		this.mJsInterface.set(this, obj);
	}

	// TODO on complete
	public void remove() {
		this.mJsInterface.remove(this);
	}

	public synchronized void push(Pushed callback) {
		this.push(new JsonObject(), callback);
	}

	public synchronized void push(JsonElement obj, Pushed callback) {
		this.mJsInterface.push(this, obj, callback);
	}

	// TODO push(obj, onComplete)

	public DataEvent on(EventType ev, DataEvent callback) {
		this.mJsInterface.on(mToString, ev, callback);
		return callback;
	}

	public void off() {
		this.mJsInterface.off(mToString);
	}

	public void off(EventType ev) {
		this.mJsInterface.off(mToString, ev);
	}

	public void off(EventType ev, DataEvent callback) {
		this.mJsInterface.off(mToString, ev, callback);
	}

	public void once(EventType ev, DataEvent callback) {
		this.mJsInterface.once(mToString, ev, callback);
	}
}
