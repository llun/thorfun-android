package th.in.llun.thorfun.api;

import org.json.JSONObject;

public abstract class JSONRemoteObject implements RemoteObject {

	protected JSONObject raw;

	public JSONRemoteObject(JSONObject raw) {
		this.raw = raw;
	}

	@Override
	public String rawString() {
		return raw.toString();
	}

	public String toString() {
		return rawString();
	}

}
