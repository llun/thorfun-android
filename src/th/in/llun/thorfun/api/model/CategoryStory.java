package th.in.llun.thorfun.api.model;

import java.util.Date;

import org.json.JSONObject;

public class CategoryStory extends JSONRemoteObject {

	public static final String SORT_HOT = "hot";
	public static final String SORT_TIME = "time";

	public static final String STATUS_NORMAL = "normal";

	public static final String PRIVACY_PUBLIC = "public";

	public static final String CATEGORY_ALL = "all";

	public CategoryStory(JSONObject raw) {
		super(raw);
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

	public int getLikeNumber() {
		return raw.optInt("like_num");
	}

	public Date getTime() {
		JSONObject time = raw.optJSONObject("time");
		return new Date(time.optLong("sec") * 1000);
	}

	public Neighbour getNeightbour() {
		JSONObject neightbour = raw.optJSONObject("neighbour");
		return new Neighbour(neightbour);
	}

	public String getUrl() {
		String username = getNeightbour().getUsername();
		String rewardId = getID();
		return String.format("http://thorfun.com/%s/story/%s", username, rewardId);
	}

}
