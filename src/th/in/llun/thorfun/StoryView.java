package th.in.llun.thorfun;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Story;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class StoryView extends Activity {

	public static final String KEY_STORY = "story";

	private CategoryStory story = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_view);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		final Activity self = this;
		Intent intent = getIntent();
		String json = intent.getStringExtra(KEY_STORY);
		try {
			story = new CategoryStory(new JSONObject(json));
		} catch (JSONException e) {
			Log.e(Thorfun.LOG_TAG, "Can't parse JSON string", e);
		}

		TextView titleView = (TextView) findViewById(R.id.story_view_title);
		TextView username = (TextView) findViewById(R.id.story_view_username_text);
		TextView like = (TextView) findViewById(R.id.story_view_favorite_text);
		TextView time = (TextView) findViewById(R.id.story_view_time_text);

		titleView.setText(Html.fromHtml(story.getTitle()));
		username.setText(story.getNeightbour().getName());
		like.setText("" + story.getLikeNumber());
		time.setText(new PrettyTime().format(story.getTime()));

		final WebView webView = (WebView) findViewById(R.id.story_view_webcontent);
		Thorfun.getInstance(this).getStory(story.getID(),
		    new ThorfunResult<Story>() {

			    @Override
			    public void onResponse(final Story response) {
				    self.runOnUiThread(new Runnable() {

					    @Override
					    public void run() {
						    StringBuilder content = new StringBuilder(response
						        .getStoryDescription());
						    Log.v(Thorfun.LOG_TAG, response.rawString());

						    webView.loadData(response.getStoryData(),
						        "text/html; charset=UTF-8", null);
					    }
				    });

			    }
		    });
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		finish();
		return true;
	}

}
