package th.in.llun.thorfun.api;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Thorfun {

	public static final String LOG_TAG = "Thorfun";

	private static Thorfun instance;

	private ExecutorService executor;
	private HttpClient client;

	public static Thorfun getInstance() {
		if (instance == null) {
			instance = new Thorfun();
		}
		return instance;
	}

	public Thorfun() {
		executor = Executors.newSingleThreadExecutor();
		client = new DefaultHttpClient();
	}

	public boolean isLoggedIn() {
		return false;
	}

	public void getStory(String id, final ThorfunResult<Story> result) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("id", id);

		invoke("http://thorfun.com/ajax/story", map, new BaseRemoteResult() {

			public void onResponse(JSONObject response) {
				result.onResponse(new Story(response));
			}

		});
	}

	public void loadStory(CategoryStory last,
	    final ThorfunResult<RemoteCollection<CategoryStory>> result) {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("category_id", CategoryStory.CATEGORY_ALL);
		map.put("following", "false");
		map.put("popular", "false");
		map.put("editor_pick", "false");
		map.put("sort", "hot");
		map.put("limit", "15");
		if (last != null) {
			map.put("skipId", last.getID());
			map.put("skipView", String.format("%d", last.getViewNumber()));
		}
		invoke("http://thorfun.com/ajax/home/story", map, new BaseRemoteResult() {

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

	public void invoke(final String url, Map<String, String> parameters,
	    final RemoteResult result) {
		final StringBuilder builder = new StringBuilder(url);

		if (parameters.size() > 0) {
			builder.append("?");
			for (String key : parameters.keySet()) {
				builder.append(String.format("%s=%s&", key, parameters.get(key)));
			}
			builder.deleteCharAt(builder.length() - 1);
		}

		executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					HttpGet get = new HttpGet(builder.toString());
					HttpResponse response = client.execute(get);
					HttpEntity entity = response.getEntity();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					entity.writeTo(baos);
					String output = baos.toString();
					Log.v(LOG_TAG, output);
					try {
						JSONObject json = new JSONObject(output);
						result.onResponse(json);
					} catch (JSONException je) {
						JSONArray json = new JSONArray(output);
						result.onResponse(json);
					}

				} catch (Exception e) {
					Log.e(LOG_TAG, e.getMessage(), e);
				}
			}
		});

	}

}
