package th.in.llun.thorfun.api;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Post;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.Story;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;

public class Thorfun {

	public static final String LOG_TAG = "Thorfun";

	public static final String CONFIG_NAME = "thorfun.network";
	public static final String CONFIG_KEY_TOKEN = "token";

	static final String METHOD_DELETE = "delete";
	static final String METHOD_POST = "post";
	static final String METHOD_PUT = "put";
	static final String METHOD_GET = "get";

	private static Thorfun sInstance;

	private ExecutorService mExecutor;
	private HttpClient mClient;
	private Context mContext;

	private String mToken = null;

	public static Thorfun getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Thorfun(context);
		}
		return sInstance;
	}

	public Thorfun(Context context) {
		mContext = context;
		mExecutor = Executors.newSingleThreadExecutor();
		mClient = new DefaultHttpClient();

		SharedPreferences preference = mContext.getSharedPreferences(CONFIG_NAME,
		    Context.MODE_PRIVATE);
		String token = preference.getString(CONFIG_KEY_TOKEN, null);
		if (token != null) {
			mToken = token;
		}
	}

	public boolean isLoggedIn() {
		return mToken != null;
	}

	public void login(String username, String password,
	    final ApiResponse<String> result) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);

		invoke("http://thorfun.com/ajax/login", METHOD_GET, map,
		    new BaseRemoteResult() {

			    public void onResponse(final String token) {
				    SharedPreferences preference = mContext.getSharedPreferences(
				        CONFIG_NAME, Context.MODE_PRIVATE);
				    Editor editor = preference.edit();
				    editor.putString(CONFIG_KEY_TOKEN, token);
				    editor.commit();

				    mToken = token;
				    result.onResponse(token);
			    }

		    });
	}

	public void logout(final ApiResponse<String> result) {

		if (mToken != null) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("token", mToken);

			invoke("http://thorfun.com/ajax/login", METHOD_DELETE, map,
			    new BaseRemoteResult() {

				    public void onResponse(String response) {
					    SharedPreferences preference = mContext.getSharedPreferences(
					        CONFIG_NAME, Context.MODE_PRIVATE);
					    Editor editor = preference.edit();
					    editor.remove(CONFIG_KEY_TOKEN);
					    editor.commit();

					    mToken = null;

					    result.onResponse(response);
				    }

			    });
		}
	}

	public void getStory(String id, final ThorfunResult<Story> result) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", id);

		jsonInvoke("http://thorfun.com/ajax/story", METHOD_GET, map,
		    new BaseRemoteResult() {

			    public void onResponse(JSONObject response) {
				    result.onResponse(new Story(response));
			    }

		    });
	}

	public void loadStory(CategoryStory lastStory,
	    final ThorfunResult<RemoteCollection<CategoryStory>> result) {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("category_id", CategoryStory.CATEGORY_ALL);
		map.put("following", "false");
		map.put("popular", "false");
		map.put("editor_pick", "false");
		map.put("sort", "hot");
		map.put("limit", "15");
		if (lastStory != null) {
			map.put("skipId", lastStory.getID());
			map.put("skipView", String.format("%d", lastStory.getViewNumber()));
		}
		jsonInvoke("http://thorfun.com/ajax/home/story", METHOD_GET, map,
		    new BaseRemoteResult() {

			    public void onResponse(JSONArray responses) {

				    ArrayList<CategoryStory> stories = new ArrayList<CategoryStory>(
				        responses.length());
				    for (int index = 0; index < responses.length(); index++) {
					    JSONObject object = responses.optJSONObject(index);
					    stories.add(new CategoryStory(object));
				    }

				    result.onResponse(new RemoteCollection<CategoryStory>(stories));
			    }

		    });

	}

	public void loadBoard(final Post lastPost,
	    final ThorfunResult<RemoteCollection<Post>> result) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("category_id", "all");
		map.put("following", "false");
		map.put("sort", "time");
		map.put("limit", "20");
		if (lastPost != null) {
			map.put("skipId", lastPost.getID());
			map.put("skipComment", String.format("%d", lastPost.getTotalComment()));
		}

		jsonInvoke("http://thorfun.com/ajax/home/board", METHOD_GET, map,
		    new BaseRemoteResult() {

			    public void onResponse(JSONArray responses) {

				    ArrayList<Post> posts = new ArrayList<Post>(responses.length());
				    for (int index = 0; index < responses.length(); index++) {
					    JSONObject object = responses.optJSONObject(index);
					    posts.add(new Post(object));
				    }

				    result.onResponse(new RemoteCollection<Post>(posts));

			    }

		    });
	}

	public void like(String storyID, final ApiResponse<String> result) {
		HashMap<String, String> map = new HashMap<String, String>(1);
		map.put("id", storyID);

		invoke("http://thorfun.com/ajax/story/like", METHOD_POST, map,
		    new BaseRemoteResult() {

			    public void onResponse(String response) {
				    result.onResponse(response);
			    }

		    });

	}

	public void unlike(String storyID, final ApiResponse<String> result) {
		HashMap<String, String> map = new HashMap<String, String>(1);
		map.put("id", storyID);

		invoke("http://thorfun.com/ajax/story/unlike", METHOD_POST, map,
		    new BaseRemoteResult() {

			    public void onResponse(String response) {
				    result.onResponse(response);
			    }

		    });
	}

	private void jsonInvoke(final String url, String method,
	    Map<String, String> parameters, final RemoteResult result) {

		invoke(url, method, parameters, new BaseRemoteResult() {

			@Override
			public void onResponse(String output) throws Exception {
				try {
					JSONObject json = new JSONObject(output);
					result.onResponse(json);
				} catch (JSONException je) {
					JSONArray json = new JSONArray(output);
					result.onResponse(json);
				}
			}

		});

	}

	private void invoke(final String url, String method,
	    Map<String, String> parameters, final RemoteResult result) {

		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>(
		    parameters.size());
		for (String key : parameters.keySet()) {
			pairs.add(new BasicNameValuePair(key, parameters.get(key)));
		}

		if (mToken != null) {
			pairs.add(new BasicNameValuePair("token", mToken));
		}

		HttpUriRequest request = null;
		if (method.equals(METHOD_DELETE) || method.equals(METHOD_GET)
		    || method == null) {
			final StringBuilder builder = new StringBuilder(url);

			if (parameters.size() > 0) {
				if (!url.endsWith("?"))
					builder.append("?");
				builder.append(URLEncodedUtils.format(pairs, "UTF-8"));
			}

			if (method.equals(METHOD_DELETE)) {
				request = new HttpDelete(builder.toString());
			} else {
				request = new HttpGet(builder.toString());
			}

		} else {
			HttpEntityEnclosingRequestBase entityEnclosing = null;
			if (method.equals(METHOD_PUT)) {
				entityEnclosing = new HttpPut(url);
			} else {
				entityEnclosing = new HttpPost(url);
			}

			try {
				entityEnclosing.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(Thorfun.LOG_TAG, "Can't set entity to request", e);
			}

			request = entityEnclosing;
		}
		final HttpUriRequest finalRequest = request;

		mExecutor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					HttpResponse response = mClient.execute(finalRequest);
					HttpEntity entity = response.getEntity();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					entity.writeTo(baos);
					final String output = baos.toString();
					Log.v(LOG_TAG, output);

					Handler handler = new Handler(mContext.getMainLooper());
					handler.post(new Runnable() {

						@Override
						public void run() {
							try {
								result.onResponse(output);
							} catch (Exception e) {
								Log.e(LOG_TAG, e.getMessage(), e);
							}
						}
					});

				} catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage(), e);
				}
			}
		});

	}
}
