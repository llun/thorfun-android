package th.in.llun.thorfun;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.adapter.ReplyAdapter;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.Comment;
import th.in.llun.thorfun.api.model.Reply;
import th.in.llun.thorfun.api.model.ThorfunResult;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CommentRepliesActivity extends Activity {

	public static final String KEY_STORY_ID = "story";
	public static final String KEY_COMMENT = "comment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_replies_view);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setTitle(getString(R.string.story_comment_reply_title));

		final String storyID = getIntent().getStringExtra(KEY_STORY_ID);
		String rawComment = getIntent().getStringExtra(KEY_COMMENT);
		try {
			final Comment comment = new Comment(new JSONObject(rawComment));

			ImageView commentAvatarImage = (ImageView) findViewById(R.id.comment_avatar);
			ViewGroup commentAvatarLoading = (ViewGroup) findViewById(R.id.comment_avatar_progress_box);
			new ImageLoader(commentAvatarImage, commentAvatarLoading).execute(comment
			    .getNeightbour().getImageURL());

			TextView usernameText = (TextView) findViewById(R.id.comment_user);
			usernameText.setText(comment.getNeightbour().getName());

			TextView commentText = (TextView) findViewById(R.id.comment_text);
			commentText.setText(Html.fromHtml(comment.getText()));

			TextView timeText = (TextView) findViewById(R.id.comment_time);
			timeText.setText(new PrettyTime().format(comment.getTime()));

			ListView repliesView = (ListView) findViewById(R.id.comment_list);
			repliesView.setAdapter(new ReplyAdapter(getLayoutInflater(), comment
			    .getReply()));

			final Activity activity = this;
			final TextView replyText = (TextView) findViewById(R.id.comment_reply_input_text);
			final Button replyButton = (Button) findViewById(R.id.comment_reply_submit);
			replyButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					replyButton.setEnabled(false);
					Thorfun.getInstance(activity).replyComment(storyID, comment,
					    replyText.getText().toString(), new ThorfunResult<Reply>() {

						    @Override
						    public void onResponse(Reply response) {
							    finish();
						    }
					    });
				}
			});
		} catch (JSONException e) {
			Log.e(Thorfun.LOG_TAG, "Cannot parse JSON string", e);
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
}
