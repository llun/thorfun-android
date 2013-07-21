package th.in.llun.thorfun.api.model;

import org.json.JSONObject;

public class Neighbour extends JSONRemoteObject {

	public Neighbour(JSONObject raw) {
		super(raw);
	}

	public String getUsername() {
		return raw.optString("username");
	}

	public String getAboutMe() {
		return raw.optString("aboutme");
	}

	public String getName() {
		return raw.optString("name");
	}

	public int getExp() {
		return raw.optInt("exp");
	}

	public int getLevel() {
		return raw.optInt("level");
	}

	public String getImageURL() {
		String prefix = "https://s3-ap-southeast-1.amazonaws.com/bucket.thorfun.com/profile_img/";
		String profile = raw.optString("image");
		return String.format("%s%s", prefix, profile);
	}

}
