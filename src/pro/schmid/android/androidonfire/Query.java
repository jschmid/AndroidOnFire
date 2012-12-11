package pro.schmid.android.androidonfire;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;

public class Query {
	private final FirebaseJavaScriptInterface mJsInterface;
	private final Firebase mFirebase;

	int mLimit = -1;
	String mStartPriority;
	String mStartName;
	String mEndPriority;
	String mEndName;

	public Query(FirebaseJavaScriptInterface jsInterface, Firebase firebase) {
		this.mJsInterface = jsInterface;
		this.mFirebase = firebase;
	}

	private Query(Query old) {
		this.mJsInterface = old.mJsInterface;
		this.mFirebase = old.mFirebase;
		this.mLimit = old.mLimit;
		this.mStartPriority = old.mStartPriority;
		this.mStartName = old.mStartName;
		this.mEndPriority = old.mEndPriority;
		this.mEndName = old.mEndName;
	}

	public Query limit(int limit) {
		Query n = new Query(this);
		n.mLimit = limit;
		return n;
	}

	public Query startAt(String priority) {
		Query n = new Query(this);
		n.mStartPriority = priority;
		return n;
	}

	public Query startAt(String priority, String name) {
		Query n = new Query(this);
		n.mStartPriority = priority;
		n.mStartName = name;
		return n;
	}

	public Query endAt(String priority) {
		Query n = new Query(this);
		n.mEndPriority = priority;
		return n;
	}

	public Query endAt(String priority, String name) {
		Query n = new Query(this);
		n.mEndPriority = priority;
		n.mEndName = name;
		return n;
	}

	public DataEvent on(EventType ev, DataEvent callback) {
		this.mJsInterface.onQuery(this.mFirebase.toString(), this, ev, callback);
		return callback;
	}
}
