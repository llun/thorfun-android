package th.in.llun.thorfun;

import th.in.llun.thorfun.api.Post;
import th.in.llun.thorfun.api.RemoteCollection;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.ThorfunResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BoardFragment extends Fragment {

	private Thorfun thorfun;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		thorfun = Thorfun.getInstance();

		View rootView = inflater.inflate(R.layout.fragment_story, container, false);
		
		return rootView;
	}
	
	public void onStart() {
		super.onStart();
		
		thorfun.loadBoard(new ThorfunResult<RemoteCollection<Post>>() {
			
			@Override
			public void onResponse(RemoteCollection<Post> response) {
				Log.d(Thorfun.LOG_TAG, response.rawString());
			}
		});
	}

}
