package pro.schmid.android.androidonfire.callbacks;

import pro.schmid.android.androidonfire.DataSnapshot;

public interface TransactionComplete {
	public void onComplete(boolean success, DataSnapshot snapshot, String reason);
}
