package pro.schmid.android.androidonfire.callbacks;

import pro.schmid.android.androidonfire.DataSnapshot;

public interface DataEvent {
	public void callback(DataSnapshot snapshot, String prevChildName);
}
