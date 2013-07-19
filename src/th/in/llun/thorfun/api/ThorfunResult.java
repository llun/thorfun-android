package th.in.llun.thorfun.api;

public interface ThorfunResult<E extends RemoteObject> {

	void onResponse(E response);
	
}
