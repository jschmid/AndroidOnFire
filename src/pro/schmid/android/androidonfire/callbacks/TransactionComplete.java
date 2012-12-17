package pro.schmid.android.androidonfire.callbacks;

import pro.schmid.android.androidonfire.DataSnapshot;

/**
 * Called when the transaction ended (could be successful or not)
 */
public interface TransactionComplete {
	public void onComplete(boolean success, DataSnapshot snapshot, String reason);
}
