package th.in.llun.thorfun.api.model;

import org.json.JSONObject;

public class Story extends JSONRemoteObject {

	public Story(JSONObject raw) {
		super(raw);
	}

	private JSONObject getStory() {
		return raw.optJSONObject("story");
	}

	public String getStoryDescription() {
		JSONObject story = getStory();
		return story.optString("desc");
	}

	public boolean isUserLiked() {
		JSONObject story = getStory();
		return story.optBoolean("is_like");
	}

	public String getStoryData() {
		JSONObject story = getStory();
		JSONObject data = story.optJSONObject("data");
		return data.optString("text");
	}

}
