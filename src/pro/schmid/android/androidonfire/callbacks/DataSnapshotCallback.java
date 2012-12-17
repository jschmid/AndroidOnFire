package pro.schmid.android.androidonfire.callbacks;

import pro.schmid.android.androidonfire.DataSnapshot;

/**
 * Callback used on a DataSnapshot when iterating over it
 */
public interface DataSnapshotCallback {
	public boolean method(DataSnapshot snapshot);
}
