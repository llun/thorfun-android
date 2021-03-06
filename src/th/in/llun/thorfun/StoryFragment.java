package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import th.in.llun.thorfun.adapter.StoryAdapter;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class StoryFragment extends SherlockFragment {

	public static final String ARGUMENT_SORTBY = "sort";

	private static final String KEY_STORIES = "stories";
	private static final String KEY_POSITION = "position";
	private static final String KEY_STATE = "state";

	private Thorfun mThorfun;
	private StoryAdapter mAdapter;
	private String mSortBy;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mSortBy = getArguments().getString(ARGUMENT_SORTBY);

		mThorfun = Thorfun.getInstance(getSherlockActivity());
		mAdapter = new StoryAdapter(getSherlockActivity(),
		    getLayoutInflater(savedInstanceState), mSortBy);

		View rootView = inflater.inflate(R.layout.fragment_story, container, false);

		final GridView grid = (GridView) rootView.findViewById(R.id.story_grid);
		final Button topButton = (Button) rootView
		    .findViewById(R.id.story_top_button);

		grid.setAdapter(mAdapter);
		grid.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
			    int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > 0) {
					topButton.setVisibility(View.VISIBLE);
				} else {
					topButton.setVisibility(View.GONE);
				}
			}
		});
		topButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				grid.smoothScrollToPosition(0);
			}
		});

		return rootView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState != null) {
			boolean state = savedInstanceState.getBoolean(KEY_STATE);
			ArrayList<String> rawStories = savedInstanceState
			    .getStringArrayList(KEY_STORIES);
			int position = savedInstanceState.getInt(KEY_POSITION);

			if (state != mThorfun.isLoggedIn()) {
				loadStories();
			} else {
				List<CategoryStory> stories = new ArrayList<CategoryStory>(
				    rawStories.size());
				for (String rawStory : rawStories) {
					try {
						CategoryStory story = new CategoryStory(new JSONObject(rawStory));
						stories.add(story);
					} catch (JSONException e) {
						Log.e(Thorfun.LOG_TAG, "Can't create CategoryStory from rawString",
						    e);
					}
				}

				mAdapter.setStories(stories);

				GridView grid = (GridView) getView().findViewById(R.id.story_grid);
				grid.scrollTo(0, position);
			}
		} else {
			loadStories();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		List<CategoryStory> stories = mAdapter.getStories();
		ArrayList<String> rawStories = new ArrayList<String>(stories.size());
		for (CategoryStory story : stories) {
			String rawStory = story.rawString();
			rawStories.add(rawStory);
		}
		outState.putStringArrayList(KEY_STORIES, rawStories);

		GridView grid = (GridView) getView().findViewById(R.id.story_grid);
		int scrollPosition = grid.getScrollY();
		outState.putInt(KEY_POSITION, scrollPosition);

		outState.putBoolean(KEY_STATE, mThorfun.isLoggedIn());

	}

	private void loadStories() {
		Log.d(Thorfun.LOG_TAG, "Load story");

		final Activity activity = getSherlockActivity();
		final RelativeLayout layout = (RelativeLayout) getView().findViewById(
		    R.id.story_loading);

		mThorfun.loadStory(null, mSortBy,
		    new ThorfunResult<RemoteCollection<CategoryStory>>() {

			    @Override
			    public void onResponse(RemoteCollection<CategoryStory> response) {
				    final List<CategoryStory> stories = response.collection();
				    activity.runOnUiThread(new Runnable() {

					    @Override
					    public void run() {
						    Log.d(Thorfun.LOG_TAG, "Loading done");
						    layout.setVisibility(View.GONE);
						    mAdapter.setStories(stories);
					    }
				    });

			    }

		    });
	}

}
