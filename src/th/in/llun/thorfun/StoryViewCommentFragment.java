package th.in.llun.thorfun;

import java.util.LinkedList;
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class StoryViewCommentFragment extends Fragment {

	public static final String KEY_COMMENTS = "comments";

	private Thorfun mThorfun;
	private CategoryStory mStory;

	private LinkedList<Comment> mComments;
	private CommentAdapter mAdapter;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_story_view_comment,
		    container, false);

		ListView commentList = (ListView) rootView
		    .findViewById(R.id.story_comment_list);

		mComments = new LinkedList<Comment>();
		mAdapter = new CommentAdapter(inflater, mComments);
		commentList.setAdapter(mAdapter);

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
			Bundle arguments = getArguments();
			try {
				mThorfun = Thorfun.getInstance(getActivity());
				mStory = new CategoryStory(new JSONObject(
				    arguments.getString(StoryView.KEY_STORY)));

				mThorfun.loadComments(mStory, null,
				    new ThorfunResult<RemoteCollection<Comment>>() {

					    @Override
					    public void onResponse(RemoteCollection<Comment> response) {
						    List<Comment> comments = response.collection();
						    mComments.addAll(comments);
						    mAdapter.notifyDataSetChanged();
					    }
				    });
			} catch (JSONException e) {
				Log.e(Thorfun.LOG_TAG, "Cannot parse JSON String", e);
			}
		}

		return rootView;
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

		private LayoutInflater mInflater;
		private List<Comment> mComments;

		public CommentAdapter(LayoutInflater inflater, List<Comment> comments) {
			mInflater = inflater;
			mComments = comments;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				row = mInflater.inflate(R.layout.story_comment_row, parent, false);
			}

			Comment comment = mComments.get(position);

			ImageView icon = (ImageView) row.findViewById(R.id.story_comment_avatar);
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

			TextView timeText = (TextView) row.findViewById(R.id.story_comment_time);
			timeText.setText(new PrettyTime().format(comment.getTime()));

			ListView repliesView = (ListView) row
			    .findViewById(R.id.story_comment_replies);
			repliesView.setAdapter(new ReplyAdapter(mInflater, comment.getReply()));
			if (comment.getReply().size() == 0) {
				repliesView.setVisibility(View.GONE);
			} else {
				repliesView.setVisibility(View.VISIBLE);
			}

			return row;
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
			return mComments.size();
		}
	}

	private static class ReplyAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<Reply> mReplies;

		public ReplyAdapter(LayoutInflater inflater, List<Reply> replies) {
			mInflater = inflater;
			mReplies = replies;
		}

		@Override
		public int getCount() {
			return mReplies.size();
		}

		@Override
		public Object getItem(int position) {
			return mReplies.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mReplies.get(position).getID();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				row = mInflater
				    .inflate(R.layout.story_comment_reply_row, parent, false);
			}

			Reply reply = mReplies.get(position);

			ImageView icon = (ImageView) row.findViewById(R.id.story_comment_avatar);
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

			TextView timeText = (TextView) row.findViewById(R.id.story_comment_time);
			timeText.setText(new PrettyTime().format(reply.getTime()));

			return row;
		}

	}

}
