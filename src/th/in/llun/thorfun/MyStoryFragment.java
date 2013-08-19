package th.in.llun.thorfun;

import java.util.List;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Neighbour;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

public class MyStoryFragment extends Fragment {
	private Thorfun mThorfun;
	private MyStoryAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mThorfun = Thorfun.getInstance(getActivity());

		mAdapter = new MyStoryAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState));

		View rootView = inflater.inflate(R.layout.fragment_story, container, false);
		GridView grid = (GridView) rootView.findViewById(R.id.story_grid);
		grid.setAdapter(mAdapter);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		final Activity activity = getActivity();
		final RelativeLayout layout = (RelativeLayout) activity
		    .findViewById(R.id.story_loading);

		Log.d(Thorfun.LOG_TAG, "Load story");
		mThorfun.getSelfNeighbour(new ThorfunResult<Neighbour>() {

			@Override
			public void onResponse(Neighbour response) {
				mAdapter.setUsername(response.getUsername());
				mThorfun.loadMyStory(null, response.getUsername(),
				    new ThorfunResult<RemoteCollection<CategoryStory>>() {

					    @Override
					    public void onResponse(RemoteCollection<CategoryStory> response) {
						    final List<CategoryStory> stories = response.collection();
						    activity.runOnUiThread(new Runnable() {

							    @Override
							    public void run() {
								    layout.setVisibility(View.GONE);
								    mAdapter.setStories(stories);
							    }
						    });

					    }

				    });
			}
		});

	}
}
