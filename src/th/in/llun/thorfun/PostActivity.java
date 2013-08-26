package th.in.llun.thorfun;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.Post;
import th.in.llun.thorfun.utils.ImageLoader;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PostActivity extends Activity {

	public static final String KEY_POST = "post";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_view);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		setTitle(getString(R.string.board_post_title));

		String rawPost = getIntent().getStringExtra(KEY_POST);
		try {
			Post post = new Post(new JSONObject(rawPost));

			ImageView icon = (ImageView) findViewById(R.id.board_post_avatar);
			ViewGroup loading = (ViewGroup) findViewById(R.id.board_post_loading);
			loading.setVisibility(View.VISIBLE);
			new ImageLoader(icon, loading)
			    .execute(post.getNeightbour().getImageURL());

			TextView title = (TextView) findViewById(R.id.board_post_title);
			title.setText(Html.fromHtml(post.getTitle()));

			TextView username = (TextView) findViewById(R.id.board_post_username_text);
			username.setText(post.getNeightbour().getName());

			PrettyTime prettyTime = new PrettyTime();
			TextView time = (TextView) findViewById(R.id.board_post_timestamp);
			time.setText(prettyTime.format(post.getTime()));
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
}
