package th.in.llun.thorfun.api;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
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

	public void login(String username, String password) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);

		invoke("http://thorfun.com/ajax/login", METHOD_GET, map,
		    new BaseRemoteResult() {

			    public void onResponse(String token) {
				    SharedPreferences preference = mContext.getSharedPreferences(
				        CONFIG_NAME, Context.MODE_PRIVATE);
				    Editor editor = preference.edit();
				    editor.putString(CONFIG_KEY_TOKEN, token);
				    editor.commit();

				    mToken = token;
			    }

		    });
	}

	public void logout() {

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
		final StringBuilder builder = new StringBuilder(url);

		HttpRequest request = null;
		if (method.equals(METHOD_DELETE)) {
			request = new HttpDelete(builder.toString());
		} else {
			request = new HttpGet(builder.toString());
		}

		HttpParams params = new BasicHttpParams();
		for (String key : parameters.keySet()) {
			params.setParameter(key, parameters.get(key));
		}
		request.setParams(params);

		mExecutor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					HttpGet get = new HttpGet(builder.toString());
					HttpResponse response = mClient.execute(get);
					HttpEntity entity = response.getEntity();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					entity.writeTo(baos);
					String output = baos.toString();
					Log.v(LOG_TAG, output);
					result.onResponse(output);

				} catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage(), e);
				}
			}
		});

	}

}
