package pro.schmid.android.androidonfire.callbacks;

/**
 * Called when data has been synchronized to the server.
 * It can be set, updated or removed.
 */
public interface SynchonizedToServer {
	public void onComplete(boolean success);
}
