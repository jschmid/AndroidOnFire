package pro.schmid.android.androidonfire;

import java.io.InputStream;

import pro.schmid.android.androidonfire.callbacks.FirebaseLoaded;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Class to be called during application initialization.
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class FirebaseEngine {

	private static final String TAG = FirebaseJavaScriptInterface.class.getSimpleName();

	private Activity mActivity;
	private WebView mWebView;
	private FirebaseJavaScriptInterface mJS;
	private FirebaseLoaded mLoadedListener;

	private ViewGroup mParentView;

	private FirebaseEngine() {
	}

	public void onDestroy() {
		mParentView.removeView(mWebView);
		mWebView.destroy();
	}

	public void loadEngine(Activity activity) {
		this.mActivity = activity;

		this.mWebView = new WebView(mActivity);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mJS = new FirebaseJavaScriptInterface(mWebView, mActivity);
		mWebView.addJavascriptInterface(mJS, "Android");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				if (mLoadedListener != null) {
					mLoadedListener.firebaseLoaded();
				}
			}
		});

		// Load the HTML from res/raw.
		// We cannot put it in assets since it is a library
		try {
			Resources res = this.mActivity.getResources();
			InputStream in_s = res.openRawResource(R.raw.firebase);

			byte[] b = new byte[in_s.available()];
			in_s.read(b);

			String content = new String(b);
			// We need to have a base URL to load JS from other domains
			mWebView.loadDataWithBaseURL("http://github.com/jschmid/AndroidOnFire", content, "text/html", "UTF-8", null);
		} catch (Exception e) {
			Log.d(TAG, "Could not load html", e);
		}

		mParentView = (ViewGroup) this.mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
		LayoutParams params = mWebView.getLayoutParams();

		if (params == null) {
			params = new LayoutParams(0, 0);
		} else {
			params.height = 0;
			params.width = 0;
		}

		mWebView.setLayoutParams(params);

		mParentView.addView(mWebView);
	}

	/**
	 * Listener to be called when the Firebase is ready and we can begin to call it.
	 * 
	 * @param loadedListener
	 */
	public void setLoadedListener(FirebaseLoaded loadedListener) {
		this.mLoadedListener = loadedListener;
	}

	/**
	 * This method should be called only once with the full Firebase URI.
	 * Subsequent calls should use the returned object and use {@link child()}
	 * 
	 * @param endpoint
	 * @return null if the endpoint is not the full URI, otherwise the {@link Firebase}
	 */
	public Firebase newFirebase(String endpoint) {
		if (!endpoint.startsWith("https://") && !endpoint.startsWith("http://")) {
			return null;
		}

		return new Firebase(mJS, endpoint);
	}

	/**
	 * Get the Singleton
	 * 
	 * @return
	 */
	public static final FirebaseEngine getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {
		public static final FirebaseEngine INSTANCE = new FirebaseEngine();
	}
}
