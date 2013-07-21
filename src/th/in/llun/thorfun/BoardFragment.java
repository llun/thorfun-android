package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Post;
import th.in.llun.thorfun.api.RemoteCollection;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.ThorfunResult;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BoardFragment extends Fragment {

	private Thorfun thorfun;
	private BoardAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		thorfun = Thorfun.getInstance();
		adapter = new BoardAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState));

		View rootView = inflater.inflate(R.layout.fragment_board, container, false);
		GridView grid = (GridView) rootView.findViewById(R.id.post_grid);
		grid.setAdapter(adapter);

		return rootView;
	}

	public void onStart() {
		super.onStart();

		final Activity activity = getActivity();
		final RelativeLayout layout = (RelativeLayout) activity
		    .findViewById(R.id.post_loading);

		thorfun.loadBoard(null, new ThorfunResult<RemoteCollection<Post>>() {

			@Override
			public void onResponse(final RemoteCollection<Post> response) {
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						layout.setVisibility(View.GONE);
						adapter.setPosts(response.collection());
					}
				});
			}
		});
	}

	private static class BoardAdapter extends BaseAdapter {

		final private List<Post> posts = new ArrayList<Post>(0);
		private LayoutInflater inflater = null;
		private Activity activity = null;

		private boolean isLoading = false;
		private boolean isLastPage = false;

		public BoardAdapter(Activity activity, LayoutInflater inflater) {
			this.activity = activity;
			this.inflater = inflater;
		}

		public void setPosts(List<Post> posts) {
			this.posts.clear();
			this.posts.addAll(posts);
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (posts.size() > 0) {
				return posts.size() + 2;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return posts.get(position - 1);
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
					row = (RelativeLayout) inflater.inflate(
					    R.layout.fragment_loading_row, parent, false);
				}

				final BaseAdapter self = this;

				if (!isLoading && !isLastPage) {
					isLoading = true;
					Post post = posts.get(posts.size() - 1);
					Thorfun.getInstance().loadBoard(post,
					    new ThorfunResult<RemoteCollection<Post>>() {

						    @Override
						    public void onResponse(RemoteCollection<Post> response) {
							    isLoading = false;

							    List<Post> next = response.collection();
							    if (next.size() > 0) {
								    posts.addAll(next);
								    activity.runOnUiThread(new Runnable() {

									    @Override
									    public void run() {
										    self.notifyDataSetChanged();
									    }
								    });
							    } else {
								    isLastPage = true;
							    }

						    };
					    });
				}

				if (isLastPage) {
					row.setVisibility(View.GONE);
				}

				return row;
			}
			// Normal row
			else {
				RelativeLayout row = (RelativeLayout) convertView;
				if (row == null) {
					row = (RelativeLayout) inflater.inflate(R.layout.fragment_post_row,
					    parent, false);
				}

				Post post = posts.get(position - 1);

				ImageView icon = (ImageView) row.findViewById(R.id.post_row_avatar);
				ViewGroup loading = (ViewGroup) row.findViewById(R.id.post_row_loading);
				loading.setVisibility(View.VISIBLE);
				new ImageLoader(icon, loading).execute(post.getNeightbour()
				    .getImageURL());

				TextView title = (TextView) row.findViewById(R.id.post_row_title);
				title.setText(Html.fromHtml(post.getTitle()));

				TextView username = (TextView) row
				    .findViewById(R.id.post_row_username_text);
				username.setText(post.getNeightbour().getName());

				TextView comment = (TextView) row
				    .findViewById(R.id.post_row_comment_text);
				String commentTemplate = activity
				    .getString(R.string.board_post_comment);
				comment.setText(commentTemplate.replace("{number}",
				    String.format("%s", post.getTotalComment())));

				PrettyTime prettyTime = new PrettyTime();
				TextView time = (TextView) row.findViewById(R.id.post_row_time_text);
				Log.d(Thorfun.LOG_TAG, "Time: " + post.getTime());
				Log.d(Thorfun.LOG_TAG, "Time: " + prettyTime.format(post.getTime()));
				time.setText(prettyTime.format(post.getTime()));

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
