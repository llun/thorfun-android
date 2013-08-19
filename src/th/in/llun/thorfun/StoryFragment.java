package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StoryFragment extends Fragment {

	private Thorfun mThorfun;
	private StoryAdapter mAdapter;
	private String mSortBy;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mThorfun = Thorfun.getInstance(getActivity());
		mAdapter = new StoryAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState), mSortBy);

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

		mSortBy = CategoryStory.SORT_HOT;
		if (mThorfun.isLoggedIn()) {
			mSortBy = CategoryStory.SORT_TIME;
		}

		mThorfun.loadStory(null, mSortBy,
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

	private static class StoryAdapter extends BaseAdapter {

		final private List<CategoryStory> mStories = new ArrayList<CategoryStory>(0);
		private LayoutInflater mInflater = null;
		private Activity mActivity = null;

		private boolean mIsLoading = false;
		private boolean mIsLastPage = false;

		private String mSortBy = CategoryStory.SORT_HOT;

		public StoryAdapter(Activity activity, LayoutInflater inflater,
		    String sortBy) {
			mActivity = activity;
			mInflater = inflater;
			mSortBy = sortBy;
		}

		public void setStories(List<CategoryStory> stories) {
			mStories.clear();
			mStories.addAll(stories);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mStories.size() > 0) {
				return mStories.size() + 2;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mStories.get(position - 1);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			if (position == 0) {
				View view = convertView;
				if (view == null) {
					view = new View(parent.getContext());
				}
				return view;
			}
			// Load next page
			else if (position == getCount() - 1) {
				RelativeLayout row = (RelativeLayout) convertView;
				if (row == null) {
					row = (RelativeLayout) mInflater.inflate(
					    R.layout.fragment_loading_row, parent, false);
				}

				final BaseAdapter self = this;

				if (!mIsLoading && !mIsLastPage) {
					mIsLoading = true;
					CategoryStory story = mStories.get(mStories.size() - 1);
					Thorfun.getInstance(mActivity).loadStory(story, mSortBy,
					    new ThorfunResult<RemoteCollection<CategoryStory>>() {

						    @Override
						    public void onResponse(RemoteCollection<CategoryStory> response) {
							    mIsLoading = false;

							    List<CategoryStory> next = response.collection();
							    if (next.size() > 0) {
								    mStories.addAll(next);
								    mActivity.runOnUiThread(new Runnable() {

									    @Override
									    public void run() {
										    self.notifyDataSetChanged();
									    }
								    });
							    } else {
								    mIsLastPage = true;
							    }

						    };
					    });
				}

				if (mIsLastPage) {
					row.setVisibility(View.GONE);
				}

				return row;
			}
			// Normal row
			else {
				RelativeLayout row = (RelativeLayout) convertView;
				if (row == null) {
					row = (RelativeLayout) mInflater.inflate(R.layout.fragment_story_row,
					    parent, false);
				}

				CategoryStory story = mStories.get(position - 1);

				ImageView icon = (ImageView) row.findViewById(R.id.story_row_icon);
				ViewGroup loading = (ViewGroup) row
				    .findViewById(R.id.story_row_icon_progress);
				loading.setVisibility(View.VISIBLE);
				new ImageLoader(icon, loading).execute(story.getImageURL());

				TextView title = (TextView) row.findViewById(R.id.story_row_title);
				TextView description = (TextView) row
				    .findViewById(R.id.story_row_description);
				TextView username = (TextView) row
				    .findViewById(R.id.story_row_username_text);
				TextView like = (TextView) row
				    .findViewById(R.id.story_row_favorite_text);
				TextView time = (TextView) row.findViewById(R.id.story_row_time_text);

				title.setText(Html.fromHtml(story.getTitle()));
				description.setText(Html.fromHtml(story.getDescription()));
				username.setText(story.getNeightbour().getName());
				like.setText(story.getLikeNumber() + "");
				time.setText(new PrettyTime().format(story.getTime()));
				row.setTag(position - 1);

				row.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						int position = (Integer) view.getTag();
						CategoryStory story = mStories.get(position);

						Intent intent = new Intent(mActivity, StoryView.class);
						intent.putExtra(StoryView.KEY_STORY, story.rawString());
						mActivity.startActivity(intent);
					}
				});

				return row;
			}
		}

		public int getViewTypeCount() {
			return 3;
		}

		public int getItemViewType(int position) {
			if (position == 0) {
				return 0;
			} else if (position == getCount() - 1) {
				return 1;
			}
			return 2;
		}
	}
}
