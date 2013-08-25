package th.in.llun.thorfun.adapter;

import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.R;
import th.in.llun.thorfun.api.model.Reply;
import th.in.llun.thorfun.utils.ImageLoader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReplyAdapter extends BaseAdapter {
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
			row = mInflater.inflate(R.layout.story_comment_reply_row, parent, false);
		}

		Reply reply = mReplies.get(position);

		ImageView icon = (ImageView) row.findViewById(R.id.story_comment_avatar);
		ViewGroup loading = (ViewGroup) row
		    .findViewById(R.id.story_comment_progress_box);
		loading.setVisibility(View.VISIBLE);
		new ImageLoader(icon, loading).execute(reply.getNeightbour().getImageURL());

		TextView usernameText = (TextView) row
		    .findViewById(R.id.story_comment_user);
		usernameText.setText(reply.getNeightbour().getName());

		TextView commentText = (TextView) row.findViewById(R.id.story_comment_text);
		commentText.setText(Html.fromHtml(reply.getText()));

		TextView timeText = (TextView) row.findViewById(R.id.story_comment_time);
		timeText.setText(new PrettyTime().format(reply.getTime()));

		return row;
	}

}
