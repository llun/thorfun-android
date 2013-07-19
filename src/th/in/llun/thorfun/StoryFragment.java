package th.in.llun.thorfun;

import th.in.llun.thorfun.api.RemoteCollection;
import th.in.llun.thorfun.api.CategoryStory;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.ThorfunResult;
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

	private Thorfun thorfun;
	private StoryAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		thorfun = Thorfun.getInstance();
		adapter = new StoryAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState));

		View rootView = inflater.inflate(R.layout.fragment_story, container, false);
		GridView grid = (GridView) rootView.findViewById(R.id.story_grid);
		grid.setAdapter(adapter);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		final Activity activity = getActivity();
		Log.d(Thorfun.LOG_TAG, "Load story");
		thorfun.loadStory(new ThorfunResult<RemoteCollection<CategoryStory>>() {

			@Override
			public void onResponse(RemoteCollection<CategoryStory> response) {
				final CategoryStory[] stories = response.collection();
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.setStories(stories);
					}
				});

			}

		});
	}

	private static class StoryAdapter extends BaseAdapter {

		private CategoryStory[] stories = new CategoryStory[0];
		private LayoutInflater inflater = null;
		private Activity activity = null;

		public StoryAdapter(Activity activity, LayoutInflater inflater) {
			this.activity = activity;
			this.inflater = inflater;
		}

		public void setStories(CategoryStory[] stories) {
			this.stories = stories;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (stories.length > 0) {
				return stories.length + 2;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return stories[position + 1];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			if (position == 0 || position == getCount() - 1) {
				View view = convertView;
				if (view == null) {
					view = new View(parent.getContext());
				}
				return view;
			}

			Log.d(Thorfun.LOG_TAG, "Position: " + position);
			RelativeLayout row = (RelativeLayout) convertView;
			if (row == null) {
				row = (RelativeLayout) inflater.inflate(R.layout.fragment_story_row,
				    parent, false);
			}

			CategoryStory story = stories[position - 1];

			ImageView icon = (ImageView) row.findViewById(R.id.story_row_icon);
			ViewGroup loading = (ViewGroup) row
			    .findViewById(R.id.story_row_icon_progress);
			loading.setVisibility(View.VISIBLE);
			new ImageLoader(icon, loading).execute(story.getImageURL());

			TextView title = (TextView) row.findViewById(R.id.story_row_title);
			TextView description = (TextView) row
			    .findViewById(R.id.story_row_description);

			title.setText(Html.fromHtml(story.getTitle()));
			description.setText(Html.fromHtml(story.getDescription()));
			row.setTag(position - 1);

			row.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					int position = (Integer) view.getTag();
					CategoryStory story = stories[position];

					Intent intent = new Intent(activity, StoryView.class);
					intent.putExtra(StoryView.KEY_STORY, story.rawString());
					activity.startActivity(intent);
				}
			});

			return row;
		}

		public int getViewTypeCount() {
			return 2;
		}

		public int getItemViewType(int position) {
			if (position == 0 || position == getCount() - 1) {
				return 0;
			}
			return 1;
		}
	}
}
