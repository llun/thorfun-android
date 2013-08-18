package th.in.llun.thorfun;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StoryViewCommentFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_story_view_comment,
		    container, false);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

}
