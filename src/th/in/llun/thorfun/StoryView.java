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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class StoryView extends Activity {

	public static final String KEY_STORY = "story";

	private CategoryStory mStory = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_view);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		String json = intent.getStringExtra(KEY_STORY);
		try {
			mStory = new CategoryStory(new JSONObject(json));
		} catch (JSONException e) {
			Log.e(Thorfun.LOG_TAG, "Can't parse JSON string", e);
		}

		TextView titleView = (TextView) findViewById(R.id.story_view_title);
		TextView username = (TextView) findViewById(R.id.story_view_username_text);
		TextView like = (TextView) findViewById(R.id.story_view_favorite_text);
		TextView time = (TextView) findViewById(R.id.story_view_time_text);

		titleView.setText(Html.fromHtml(mStory.getTitle()));
		username.setText(mStory.getNeightbour().getName());
		like.setText("" + mStory.getLikeNumber());
		time.setText(new PrettyTime().format(mStory.getTime()));

		final WebView webView = (WebView) findViewById(R.id.story_view_webcontent);
		Thorfun.getInstance(this).getStory(mStory.getID(),
		    new ThorfunResult<Story>() {

			    @Override
			    public void onResponse(final Story response) {
				    StringBuilder content = new StringBuilder(response
				        .getStoryDescription().trim());

				    if (content.length() > 0) {
					    content.append("\n<br /><hr /><br />\n");
				    }

				    content.append(response.getStoryData());

				    webView.loadData(content.toString(), "text/html; charset=UTF-8",
				        null);

			    }
		    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.story_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.story_menu_share:
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, mStory.getUrl()
			    + " #thorfun #android");
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent, "Share"));
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
