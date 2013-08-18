package th.in.llun.thorfun.api.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Comment extends JSONRemoteObject {

	public Comment(JSONObject raw) {
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

	public List<Reply> getReply() {
		LinkedList<Reply> replyList = new LinkedList<Reply>();
		JSONArray replies = raw.optJSONArray("reply");
		for (int i = 0; i < replies.length(); i++) {
			JSONObject reply = replies.optJSONObject(i);
			replyList.add(new Reply(reply));
		}
		return replyList;
	}

}
