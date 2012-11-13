package pro.schmid.android.androidonfire;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataSnapshot {
	private final Firebase mParent;
	private final JsonElement mElement;

	public DataSnapshot(Firebase parent) {
		this.mParent = parent;
		this.mElement = null;
	}

	public DataSnapshot(Firebase parent, JsonElement el) {
		this.mParent = parent;
		this.mElement = el;
	}

	public DataSnapshot(Firebase parent, String el) {
		this.mParent = parent;
		this.mElement = SingletonHolder.INSTANCE.parse(el);
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

	private static class SingletonHolder {
		public static final JsonParser INSTANCE = new JsonParser();
	}

	public static interface DataSnapshotCallback {
		public boolean method(DataSnapshot snapshot);
	}
}
