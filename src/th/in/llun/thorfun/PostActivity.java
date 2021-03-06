package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.Post;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.Reply;
import th.in.llun.thorfun.api.model.ThorfunResult;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PostActivity extends SherlockActivity {

	public static final String KEY_POST = "post";

	private Thorfun mThorfun;

	private Post mPost;
	private List<Reply> mReplies;
	private ReplyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_view);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setTitle(getString(R.string.board_post_title));

		String rawPost = getIntent().getStringExtra(KEY_POST);
		try {
			mPost = new Post(new JSONObject(rawPost));

			ImageView icon = (ImageView) findViewById(R.id.board_post_avatar);
			ViewGroup loading = (ViewGroup) findViewById(R.id.board_post_loading);
			loading.setVisibility(View.VISIBLE);
			new ImageLoader(icon, loading).execute(mPost.getNeightbour()
			    .getImageURL());

			TextView title = (TextView) findViewById(R.id.board_post_title);
			title.setText(Html.fromHtml(mPost.getTitle()));

			TextView username = (TextView) findViewById(R.id.board_post_username_text);
			username.setText(mPost.getNeightbour().getName());

			PrettyTime prettyTime = new PrettyTime();
			TextView time = (TextView) findViewById(R.id.board_post_timestamp);
			time.setText(prettyTime.format(mPost.getTime()));

			mThorfun = Thorfun.getInstance(this);
			mReplies = new ArrayList<Reply>();
			mAdapter = new ReplyAdapter(this, getLayoutInflater(), mPost, mReplies);
			ListView repliesView = (ListView) findViewById(R.id.board_post_replies);
			repliesView.setAdapter(mAdapter);
			mThorfun.loadPostReplies(mPost, null,
			    new ThorfunResult<RemoteCollection<Reply>>() {

				    @Override
				    public void onResponse(RemoteCollection<Reply> response) {
					    mReplies.addAll(response.collection());
					    mAdapter.notifyDataSetChanged();
				    }
			    });

			View inputView = findViewById(R.id.board_post_reply_field);
			if (mThorfun.isLoggedIn()) {
				inputView.setVisibility(View.VISIBLE);
			} else {
				inputView.setVisibility(View.GONE);
			}

			final EditText inputField = (EditText) findViewById(R.id.board_post_reply_input);
			final Button inputButton = (Button) findViewById(R.id.board_post_reply_submit);
			inputButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String text = inputField.getText().toString().trim();
					if (text.length() > 0) {
						inputField.setText("");
						inputButton.setEnabled(false);
						mThorfun.commentPost(mPost, text, new ThorfunResult<Reply>() {

							@Override
							public void onResponse(Reply response) {
								inputButton.setEnabled(true);
								mReplies.add(response);
								mAdapter.notifyDataSetChanged();
							}

						});
					}
				}
			});

		} catch (JSONException e) {
			Log.e(Thorfun.LOG_TAG, "Can't parse post json", e);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static class ReplyAdapter extends BaseAdapter {

		private Activity mActivity;
		private LayoutInflater mInflater;
		private Post mPost;
		private List<Reply> mReplies;
		private boolean mIsLoading = false;
		private boolean mIsLastPage = false;

		public ReplyAdapter(Activity activity, LayoutInflater inflater, Post post,
		    List<Reply> replies) {
			mActivity = activity;
			mInflater = inflater;
			mPost = post;
			mReplies = replies;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == getCount() - 1) {
				RelativeLayout row = (RelativeLayout) convertView;
				if (row == null) {
					row = (RelativeLayout) mInflater.inflate(
					    R.layout.fragment_loading_row, parent, false);
				}

				if (mReplies.size() < Thorfun.DEFAULT_PAGE_LIMIT) {
					mIsLastPage = true;
				}

				final BaseAdapter self = this;

				if (!mIsLoading && !mIsLastPage) {
					mIsLoading = true;
					Thorfun.getInstance(mActivity).loadPostReplies(mPost,
					    mReplies.get(mReplies.size() - 1),
					    new ThorfunResult<RemoteCollection<Reply>>() {

						    @Override
						    public void onResponse(RemoteCollection<Reply> response) {
							    mIsLoading = false;

							    List<Reply> next = response.collection();
							    if (next.size() > 0) {
								    mReplies.addAll(next);
								    mActivity.runOnUiThread(new Runnable() {

									    @Override
									    public void run() {
										    self.notifyDataSetChanged();
									    }
								    });
							    } else {
								    mIsLastPage = true;
							    }
						    }
					    });
				}

				if (mIsLastPage) {
					row.setVisibility(View.GONE);
				}

				return row;
			} else {
				View row = convertView;
				if (row == null) {
					row = mInflater.inflate(R.layout.story_comment_reply_row, parent,
					    false);
				}

				Reply reply = mReplies.get(position);

				ImageView icon = (ImageView) row
				    .findViewById(R.id.story_comment_avatar);
				ViewGroup loading = (ViewGroup) row
				    .findViewById(R.id.story_comment_progress_box);
				loading.setVisibility(View.VISIBLE);
				new ImageLoader(icon, loading).execute(reply.getNeightbour()
				    .getImageURL());

				TextView usernameText = (TextView) row
				    .findViewById(R.id.story_comment_user);
				usernameText.setText(reply.getNeightbour().getName());

				TextView commentText = (TextView) row
				    .findViewById(R.id.story_comment_text);
				commentText.setText(Html.fromHtml(reply.getText()));

				TextView timeText = (TextView) row
				    .findViewById(R.id.story_comment_time);
				timeText.setText(new PrettyTime().format(reply.getTime()));

				return row;
			}

		}

		@Override
		public long getItemId(int position) {
			return mReplies.get(position).getID();
		}

		@Override
		public Object getItem(int position) {
			return mReplies.get(position);
		}

		@Override
		public int getCount() {
			if (mReplies.size() > 0) {
				return mReplies.size() + 1;
			}
			return 0;
		}

		public int getViewTypeCount() {
			return 2;
		}

		public int getItemViewType(int position) {
			if (position == getCount() - 1) {
				return 0;
			}
			return 1;
		}
	}
}
