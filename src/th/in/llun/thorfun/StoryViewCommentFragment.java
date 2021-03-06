package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Comment;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.Reply;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class StoryViewCommentFragment extends SherlockFragment {

	public static final String KEY_COMMENTS = "comments";

	private Thorfun mThorfun;
	private CategoryStory mStory;

	private List<Comment> mComments;
	private CommentAdapter mAdapter;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_story_view_comment,
		    container, false);

		Bundle arguments = getArguments();
		try {
			mStory = new CategoryStory(new JSONObject(
			    arguments.getString(StoryView.KEY_STORY)));

			ListView commentList = (ListView) rootView
			    .findViewById(R.id.story_comment_list);

			mThorfun = Thorfun.getInstance(getActivity());
			mComments = new ArrayList<Comment>();
			mAdapter = new CommentAdapter(getActivity(), inflater, mStory, mComments);
			commentList.setAdapter(mAdapter);

			View commentBox = rootView.findViewById(R.id.story_comment_field);
			if (mThorfun.isLoggedIn()) {
				commentBox.setVisibility(View.VISIBLE);
			} else {
				commentBox.setVisibility(View.GONE);
			}

			final EditText commentField = (EditText) commentBox
			    .findViewById(R.id.story_comment_input_text);
			final Button commentButton = (Button) commentBox
			    .findViewById(R.id.story_comment_submit);
			commentButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String input = commentField.getText().toString();
					commentField.setText("");
					commentButton.setEnabled(false);

					mThorfun.comment(mStory, input, new ThorfunResult<Comment>() {

						@Override
						public void onResponse(Comment response) {
							reloadComment();
							commentButton.setEnabled(true);
						}
					});
				}
			});

			if (savedInstanceState != null) {
				String serialize = savedInstanceState.getString(KEY_COMMENTS);
				try {
					JSONArray array = new JSONArray(serialize);
					for (int i = 0; i < array.length(); i++) {
						mComments.add(new Comment(array.optJSONObject(i)));

					}
					mAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					Log.e(Thorfun.LOG_TAG, "Cannot parse JSON String", e);
				}

			} else {
				reloadComment();
			}
		} catch (JSONException e1) {
			Log.e(Thorfun.LOG_TAG, "Cannot parse arguments", e1);
		}

		return rootView;
	}

	private void reloadComment() {
		mThorfun.loadComments(mStory, null,
		    new ThorfunResult<RemoteCollection<Comment>>() {

			    @Override
			    public void onResponse(RemoteCollection<Comment> response) {
				    List<Comment> comments = response.collection();
				    if (comments.size() > 0) {
					    mComments.clear();
					    mComments.addAll(comments);
					    mAdapter.notifyDataSetChanged();
				    } else {
					    Activity activity = getActivity();
					    View emptyView = activity.findViewById(R.id.story_comment_empty);
					    emptyView.setVisibility(View.VISIBLE);

					    View listView = activity.findViewById(R.id.story_comment_list);
					    listView.setVisibility(View.INVISIBLE);

				    }
			    }
		    });
	}

	public void onResume() {
		super.onResume();
		reloadComment();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		StringBuilder serialize = new StringBuilder("[");
		for (Comment comment : mComments) {
			serialize.append(String.format("%s,", comment.rawString()));
		}
		serialize.append("]");
		outState.putString(KEY_COMMENTS, serialize.toString());
	}

	private static class CommentAdapter extends BaseAdapter {

		private Activity mActivity;
		private LayoutInflater mInflater;
		private CategoryStory mStory;
		private List<Comment> mComments;
		private boolean mIsLoading = false;
		private boolean mIsLastPage = false;

		public CommentAdapter(Activity activity, LayoutInflater inflater,
		    CategoryStory story, List<Comment> comments) {
			mActivity = activity;
			mInflater = inflater;
			mStory = story;
			mComments = comments;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == getCount() - 1) {
				RelativeLayout row = (RelativeLayout) convertView;
				if (row == null) {
					row = (RelativeLayout) mInflater.inflate(
					    R.layout.fragment_loading_row, parent, false);
				}

				if (mComments.size() < Thorfun.DEFAULT_PAGE_LIMIT) {
					mIsLastPage = true;
				}

				final BaseAdapter self = this;

				if (!mIsLoading && !mIsLastPage) {
					mIsLoading = true;
					Thorfun.getInstance(mActivity).loadComments(mStory,
					    mComments.get(mComments.size() - 1),
					    new ThorfunResult<RemoteCollection<Comment>>() {

						    @Override
						    public void onResponse(RemoteCollection<Comment> response) {
							    mIsLoading = false;

							    List<Comment> next = response.collection();
							    if (next.size() > 0) {
								    mComments.addAll(next);
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
					row = mInflater.inflate(R.layout.story_comment_row, parent, false);
				}

				final Comment comment = mComments.get(position);

				ImageView icon = (ImageView) row
				    .findViewById(R.id.story_comment_avatar);
				ViewGroup loading = (ViewGroup) row
				    .findViewById(R.id.story_comment_progress_box);
				loading.setVisibility(View.VISIBLE);
				new ImageLoader(icon, loading).execute(comment.getNeightbour()
				    .getImageURL());

				TextView usernameText = (TextView) row
				    .findViewById(R.id.story_comment_user);
				usernameText.setText(comment.getNeightbour().getName());

				TextView commentText = (TextView) row
				    .findViewById(R.id.story_comment_text);
				commentText.setText(Html.fromHtml(comment.getText()));

				TextView timeText = (TextView) row
				    .findViewById(R.id.story_comment_time);
				timeText.setText(new PrettyTime().format(comment.getTime()));

				LinearLayout repliesView = (LinearLayout) row
				    .findViewById(R.id.story_comment_replies);
				if (comment.getReply().size() == 0) {
					repliesView.setVisibility(View.GONE);
				} else {
					repliesView.setVisibility(View.VISIBLE);
					repliesView.removeAllViews();

					for (Reply reply : comment.getReply()) {
						View replyView = mInflater.inflate(
						    R.layout.story_comment_reply_row, parent, false);
						ImageView replyIcon = (ImageView) replyView
						    .findViewById(R.id.story_comment_avatar);
						ViewGroup replyIconLoading = (ViewGroup) replyView
						    .findViewById(R.id.story_comment_progress_box);
						replyIconLoading.setVisibility(View.VISIBLE);
						new ImageLoader(replyIcon, replyIconLoading).execute(reply
						    .getNeightbour().getImageURL());

						TextView replyUsernameText = (TextView) replyView
						    .findViewById(R.id.story_comment_user);
						replyUsernameText.setText(reply.getNeightbour().getName());

						TextView replyCommentText = (TextView) replyView
						    .findViewById(R.id.story_comment_text);
						replyCommentText.setText(Html.fromHtml(reply.getText()));

						TextView replyTimeText = (TextView) replyView
						    .findViewById(R.id.story_comment_time);
						replyTimeText.setText(new PrettyTime().format(reply.getTime()));
						repliesView.addView(replyView);
					}
				}

				if (Thorfun.getInstance(mActivity).isLoggedIn()) {
					row.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mActivity,
							    CommentRepliesActivity.class);
							intent.putExtra(CommentRepliesActivity.KEY_COMMENT,
							    comment.rawString());
							intent.putExtra(CommentRepliesActivity.KEY_STORY_ID,
							    mStory.getID());
							mActivity.startActivity(intent);

						}
					});
				}

				return row;
			}

		}

		@Override
		public long getItemId(int position) {
			return mComments.get(position).getID();
		}

		@Override
		public Object getItem(int position) {
			return mComments.get(position);
		}

		@Override
		public int getCount() {
			if (mComments.size() > 0) {
				return mComments.size() + 1;
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
