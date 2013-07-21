package th.in.llun.thorfun.api.model;

public interface ThorfunResult<E extends RemoteObject> {

	void onResponse(E response);
	
}
