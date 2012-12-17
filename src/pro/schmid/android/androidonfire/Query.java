package pro.schmid.android.androidonfire;

import pro.schmid.android.androidonfire.callbacks.DataEvent;
import pro.schmid.android.androidonfire.callbacks.EventType;

public class Query {
	private final FirebaseJavaScriptInterface mJsInterface;
	private final Firebase mFirebase;

	int mLimit = -1;
	Boolean mStartSpecified = false;
	String mStartPriority;
	String mStartName;
	Boolean mEndSpecified = false;
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

	public Query startAt() {
		return startAt(null, null);
	}

	public Query startAt(String priority) {
		return startAt(priority, null);
	}

	public Query startAt(String priority, String name) {
		Query n = new Query(this);
		n.mStartSpecified = true;
		n.mStartPriority = priority;
		n.mStartName = name;
		return n;
	}

	public Query endAt() {
		return endAt(null, null);
	}

	public Query endAt(String priority) {
		return endAt(priority, null);
	}

	public Query endAt(String priority, String name) {
		Query n = new Query(this);
		n.mEndSpecified = true;
		n.mEndPriority = priority;
		n.mEndName = name;
		return n;
	}

	public DataEvent on(EventType ev, DataEvent callback) {
		this.mJsInterface.onQuery(this.mFirebase.toString(), this, ev, callback);
		return callback;
	}

	public void off() {
		this.mJsInterface.offQuery(this.mFirebase.toString(), this);
	}

	public void off(EventType ev) {
		this.mJsInterface.offQuery(this.mFirebase.toString(), this, ev);
	}

	public void off(EventType ev, DataEvent callback) {
		this.mJsInterface.offQuery(this.mFirebase.toString(), this, ev, callback);
	}

	public void once(EventType ev, DataEvent callback) {
		this.mJsInterface.onceQuery(this.mFirebase.toString(), this, ev, callback);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mEndName == null ? 0 : mEndName.hashCode());
		result = prime * result + (mEndPriority == null ? 0 : mEndPriority.hashCode());
		result = prime * result + (mEndSpecified == null ? 0 : mEndSpecified.hashCode());
		result = prime * result + mLimit;
		result = prime * result + (mStartName == null ? 0 : mStartName.hashCode());
		result = prime * result + (mStartPriority == null ? 0 : mStartPriority.hashCode());
		result = prime * result + (mStartSpecified == null ? 0 : mStartSpecified.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Query other = (Query) obj;
		if (mEndName == null) {
			if (other.mEndName != null) {
				return false;
			}
		} else if (!mEndName.equals(other.mEndName)) {
			return false;
		}
		if (mEndPriority == null) {
			if (other.mEndPriority != null) {
				return false;
			}
		} else if (!mEndPriority.equals(other.mEndPriority)) {
			return false;
		}
		if (mEndSpecified == null) {
			if (other.mEndSpecified != null) {
				return false;
			}
		} else if (!mEndSpecified.equals(other.mEndSpecified)) {
			return false;
		}
		if (mLimit != other.mLimit) {
			return false;
		}
		if (mStartName == null) {
			if (other.mStartName != null) {
				return false;
			}
		} else if (!mStartName.equals(other.mStartName)) {
			return false;
		}
		if (mStartPriority == null) {
			if (other.mStartPriority != null) {
				return false;
			}
		} else if (!mStartPriority.equals(other.mStartPriority)) {
			return false;
		}
		if (mStartSpecified == null) {
			if (other.mStartSpecified != null) {
				return false;
			}
		} else if (!mStartSpecified.equals(other.mStartSpecified)) {
			return false;
		}
		return true;
	}
}
