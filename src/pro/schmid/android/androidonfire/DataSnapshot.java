package pro.schmid.android.androidonfire;

import java.util.Map.Entry;

import pro.schmid.android.androidonfire.callbacks.DataSnapshotCallback;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a snapshot of data.
 * It can only be constructed by the Engine.
 * See {@link https://www.firebase.com/docs/javascript-client/datasnapshot/index.html}
 */
public class DataSnapshot {
	private final Firebase mParent;
	private final JsonElement mElement;
	private final String mPriority;

	DataSnapshot(Firebase parent) {
		this(parent, (JsonElement) null, null);
	}

	DataSnapshot(Firebase parent, String el) {
		this(parent, el, null);
	}

	DataSnapshot(Firebase parent, String el, String priority) {
		this(parent, FirebaseJsonParser.getInstance().parse(el), priority);
	}

	DataSnapshot(Firebase parent, JsonElement el) {
		this(parent, el, null);
	}

	DataSnapshot(Firebase parent, JsonElement el, String priority) {
		this.mParent = parent;
		this.mElement = el;
		this.mPriority = priority;
	}

	public JsonElement val() {
		return this.mElement;
	}

	// TODO path instead of name
	public DataSnapshot child(String childPath) {
		Firebase firebaseChild = this.mParent.child(childPath);

		if (!hasChildren()) {
			return new DataSnapshot(firebaseChild);
		}

		JsonElement child = this.mElement.getAsJsonObject().get(childPath);
		return new DataSnapshot(firebaseChild, child);
	}

	/**
	 * Performs an action for every child in the current Firebase.
	 * 
	 * @param childAction
	 *            The callback called for each child.
	 * @return true is the callback has been ended
	 */
	// TODO priority
	public boolean forEach(DataSnapshotCallback childAction) {
		if (!hasChildren()) {
			return false;
		}

		JsonObject obj = this.mElement.getAsJsonObject();

		for (Entry<String, JsonElement> item : obj.entrySet()) {
			Firebase firebaseChild = this.mParent.child(item.getKey());

			if (childAction.method(new DataSnapshot(firebaseChild, item.getValue()))) {
				return true;
			}
		}

		return false;
	}

	// TODO child path
	public boolean hasChild(String childPath) {
		if (hasChildren()) {
			return this.mElement.getAsJsonObject().has(childPath);
		}

		return false;
	}

	public boolean hasChildren() {
		return this.mElement != null && this.mElement.isJsonObject();
	}

	public String name() {
		return this.mParent.name();
	}

	public int numChildren() {
		if (!hasChildren()) {
			return 0;
		}

		return this.mElement.getAsJsonObject().entrySet().size();
	}

	public Firebase ref() {
		return this.mParent;
	}

	public String getPriority() {
		return mPriority;
	}
}
