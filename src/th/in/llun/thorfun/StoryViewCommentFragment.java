package th.in.llun.thorfun;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Comment;
import th.in.llun.thorfun.api.model.RemoteCollection;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

		LayoutInflater mInflater;
		List<Comment> mComments;

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

			TextView commentText = (TextView) row
			    .findViewById(R.id.story_comment_text);
			commentText.setText(mComments.get(position).getText());

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

}
