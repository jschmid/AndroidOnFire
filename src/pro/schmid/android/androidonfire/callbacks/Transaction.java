package pro.schmid.android.androidonfire.callbacks;

import com.google.gson.JsonElement;

public interface Transaction {
	/**
	 * Return the value to update the Firebase. Return null to cancel the transaction. Return JsonNull to really set the value to "null".
	 * 
	 * @param obj
	 * @return
	 */
	public JsonElement transaction(JsonElement obj);
}
