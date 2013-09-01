package th.in.llun.thorfun;

import java.util.List;

import th.in.llun.thorfun.adapter.MyStoryAdapter;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Neighbour;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class MyStoryFragment extends SherlockFragment {
	private Thorfun mThorfun;
	private MyStoryAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mThorfun = Thorfun.getInstance(getActivity());

		mAdapter = new MyStoryAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState));

		View rootView = inflater.inflate(R.layout.fragment_my_story, container, false);
		GridView grid = (GridView) rootView.findViewById(R.id.my_story_grid);
		grid.setAdapter(mAdapter);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		final Activity activity = getActivity();
		final RelativeLayout layout = (RelativeLayout) activity
		    .findViewById(R.id.my_story_loading);

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
