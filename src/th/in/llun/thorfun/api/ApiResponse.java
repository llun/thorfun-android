package th.in.llun.thorfun.api;

public interface ApiResponse<E extends Object> {

	void onError(Exception exception);
	void onResponse(E result);
	
}
