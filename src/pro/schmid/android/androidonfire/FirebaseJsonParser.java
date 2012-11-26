package pro.schmid.android.androidonfire;

import com.google.gson.JsonParser;

public class FirebaseJsonParser {

	public static final JsonParser getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		public static final JsonParser INSTANCE = new JsonParser();
	}
}
