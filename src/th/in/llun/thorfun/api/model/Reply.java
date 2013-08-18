package th.in.llun.thorfun.api.model;

import java.util.Date;

import org.json.JSONObject;

public class Reply extends JSONRemoteObject {

	public Reply(JSONObject raw) {
		super(raw);
	}

	public int getID() {
		return raw.optInt("_id");
	}

	public String getText() {
		return raw.optString("text");
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
