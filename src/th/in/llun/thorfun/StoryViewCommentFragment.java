package th.in.llun.thorfun;

import org.json.JSONException;
import org.json.JSONObject;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Comment;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StoryViewCommentFragment extends Fragment {

	private Thorfun mThorfun;
	private CategoryStory mStory;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_story_view_comment,
		    container, false);

		Bundle arguments = getArguments();
		try {
			mThorfun = Thorfun.getInstance(getActivity());
			mStory = new CategoryStory(new JSONObject(
			    arguments.getString(StoryView.KEY_STORY)));

			mThorfun.loadComments(mStory, null,
			    new ThorfunResult<RemoteCollection<Comment>>() {

				    @Override
				    public void onResponse(RemoteCollection<Comment> response) {
					    Log.d(Thorfun.LOG_TAG, response.rawString());
				    }
			    });
		} catch (JSONException e) {
			Log.e(Thorfun.LOG_TAG, "Cannot parse JSON String", e);
		}

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

	}

}
