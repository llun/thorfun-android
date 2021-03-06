package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.Post;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.actionbarsherlock.app.SherlockFragment;

public class BoardFragment extends SherlockFragment {

	private static final String KEY_POSTS = "posts";
	private static final String KEY_POSITION = "position";

	private Thorfun mThorfun;
	private BoardAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		mThorfun = Thorfun.getInstance(getActivity());
		mAdapter = new BoardAdapter(getActivity(),
		    getLayoutInflater(savedInstanceState));

		View rootView = inflater.inflate(R.layout.fragment_board, container, false);
		final GridView grid = (GridView) rootView.findViewById(R.id.post_grid);
		grid.setAdapter(mAdapter);
		
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

		View inputView = rootView.findViewById(R.id.post_field);
		if (mThorfun.isLoggedIn()) {
			inputView.setVisibility(View.VISIBLE);
		} else {
			inputView.setVisibility(View.GONE);
		}

		final EditText inputField = (EditText) inputView
		    .findViewById(R.id.post_field_input);
		final Button submitButton = (Button) inputView
		    .findViewById(R.id.post_field_submit);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String title = inputField.getText().toString().trim();
				if (title.length() > 0) {
					inputField.setText("");
					submitButton.setEnabled(false);
					mThorfun.postBoard(title, new ThorfunResult<Post>() {

						@Override
						public void onResponse(Post response) {
							mThorfun.loadBoard(null,
							    new ThorfunResult<RemoteCollection<Post>>() {

								    @Override
								    public void onResponse(final RemoteCollection<Post> response) {
									    submitButton.setEnabled(true);
									    mAdapter.setPosts(response.collection());
								    }
							    });
						}

					});
				}
			}
		});

		return rootView;
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState != null) {
			ArrayList<String> rawPosts = savedInstanceState
			    .getStringArrayList(KEY_POSTS);
			List<Post> posts = new ArrayList<Post>(rawPosts.size());
			for (String rawPost : rawPosts) {
				try {
					posts.add(new Post(new JSONObject(rawPost)));
				} catch (JSONException e) {
					Log.e(Thorfun.LOG_TAG, "Can't parse Post JSON data", e);
				}
			}
			mAdapter.setPosts(posts);

			int position = savedInstanceState.getInt(KEY_POSITION);
			GridView grid = (GridView) getView().findViewById(R.id.post_grid);
			grid.scrollTo(0, position);
		} else {
			loadPosts();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		List<Post> posts = mAdapter.getPosts();
		ArrayList<String> rawPosts = new ArrayList<String>(posts.size());
		for (Post post : posts) {
			rawPosts.add(post.rawString());
		}
		outState.putStringArrayList(KEY_POSTS, rawPosts);

		GridView grid = (GridView) getView().findViewById(R.id.post_grid);
		outState.putInt(KEY_POSITION, grid.getScrollY());

	}

	private void loadPosts() {
		final Activity activity = getActivity();
		final RelativeLayout layout = (RelativeLayout) activity
		    .findViewById(R.id.post_loading);

		mThorfun.loadBoard(null, new ThorfunResult<RemoteCollection<Post>>() {

			@Override
			public void onResponse(final RemoteCollection<Post> response) {
				layout.setVisibility(View.GONE);
				mAdapter.setPosts(response.collection());
			}
		});
	}

	private static class BoardAdapter extends BaseAdapter {

		final private List<Post> mPosts = new ArrayList<Post>(0);
		private LayoutInflater mInflater = null;
		private Activity mActivity = null;

		private boolean isLoading = false;
		private boolean isLastPage = false;

		public BoardAdapter(Activity activity, LayoutInflater inflater) {
			mActivity = activity;
			mInflater = inflater;
		}

		public void setPosts(List<Post> posts) {
			mPosts.clear();
			mPosts.addAll(posts);
			notifyDataSetChanged();
		}

		public List<Post> getPosts() {
			return mPosts;
		}

		@Override
		public int getCount() {
			if (mPosts.size() > 0) {
				return mPosts.size() + 2;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mPosts.get(position - 1);
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

				if (mPosts.size() < Thorfun.DEFAULT_PAGE_LIMIT) {
					isLastPage = true;
				}

				final BaseAdapter self = this;

				if (!isLoading && !isLastPage) {
					isLoading = true;
					Post post = mPosts.get(mPosts.size() - 1);
					Thorfun.getInstance(mActivity).loadBoard(post,
					    new ThorfunResult<RemoteCollection<Post>>() {

						    @Override
						    public void onResponse(RemoteCollection<Post> response) {
							    isLoading = false;

							    List<Post> next = response.collection();
							    if (next.size() > 0) {
								    mPosts.addAll(next);
								    self.notifyDataSetChanged();
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
					row = (RelativeLayout) mInflater.inflate(R.layout.fragment_post_row,
					    parent, false);
				}

				final Post post = mPosts.get(position - 1);

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
				String commentTemplate = mActivity
				    .getString(R.string.board_post_comment);
				comment.setText(commentTemplate.replace("{number}",
				    String.format("%s", post.getTotalComment())));

				PrettyTime prettyTime = new PrettyTime();
				TextView time = (TextView) row.findViewById(R.id.post_row_time_text);
				time.setText(prettyTime.format(post.getTime()));

				row.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mActivity, PostActivity.class);
						intent.putExtra(PostActivity.KEY_POST, post.rawString());
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
