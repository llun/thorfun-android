package th.in.llun.thorfun.api;

import java.util.Date;

import org.json.JSONObject;

import android.util.Log;

public class CategoryStory extends JSONRemoteObject {

	public static final String STATUS_NORMAL = "normal";

	public static final String PRIVACY_PUBLIC = "public";

	public static final String CATEGORY_ALL = "all";

	public CategoryStory(JSONObject raw) {
		super(raw);

		Log.d(Thorfun.LOG_TAG, raw.toString());
	}

	public String getID() {
		JSONObject id = raw.optJSONObject("_id");
		return id.optString("$id");
	}

	public String getTitle() {
		return raw.optString("title");
	}

	public String getDescription() {
		return raw.optString("desc");
	}

	public String getStatus() {
		return raw.optString("status");
	}

	public String getPrivacy() {
		return raw.optString("privacy");
	}

	public String getImageURL() {
		return raw.optString("image");
	}

	public int getViewNumber() {
		return raw.optInt("view_num");
	}

	public Date getTime() {
		JSONObject time = raw.optJSONObject("time");
		return new Date(time.optLong("sec"));
	}

	public Neighbour getNeightbour() {
		JSONObject neightbour = raw.optJSONObject("neighbour");
		return new Neighbour(neightbour);
	}

}
