package pro.schmid.android.androidonfire.callbacks;

import pro.schmid.android.androidonfire.DataSnapshot;

/**
 * Callback when an event happened on a Firebase.
 */
public interface DataEvent {
	public void callback(DataSnapshot snapshot, String prevChildName);
}
