package th.in.llun.thorfun.api;

import org.json.JSONObject;

public class Post extends JSONRemoteObject {

	public Post(JSONObject raw) {
		super(raw);
	}

	public String getID() {
		JSONObject id = raw.optJSONObject("_id");
		return id.optString("$id");
	}

	public String getTitle() {
		return raw.optString("title");
	}
	
	public int getTotalComment() {
		return raw.optInt("comment_num");
	}

	public Neighbour getNeightbour() {
		JSONObject neightbour = raw.optJSONObject("neighbour");
		return new Neighbour(neightbour);
	}

}
