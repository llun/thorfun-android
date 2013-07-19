package th.in.llun.thorfun.api;

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

}
