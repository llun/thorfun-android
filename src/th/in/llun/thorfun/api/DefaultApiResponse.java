package th.in.llun.thorfun.api;

import android.util.Log;

public class DefaultApiResponse<E extends Object> implements ApiResponse<E> {

	@Override
	public void onError(Exception exception) {
		Log.e(Thorfun.LOG_TAG, "Error response", exception);
	}

	@Override
	public void onResponse(E result) {
		Log.v(Thorfun.LOG_TAG, "Result: " + result.toString());
	}

}
