package th.in.llun.thorfun.api.model;

import java.util.Date;

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

	public Date getTime() {
		JSONObject time = raw.optJSONObject("time");
		return new Date(time.optLong("sec") * 1000);
	}

	public Neighbour getNeightbour() {
		JSONObject neightbour = raw.optJSONObject("neighbour");
		return new Neighbour(neightbour);
	}

}
