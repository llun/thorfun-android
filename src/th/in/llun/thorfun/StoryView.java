package th.in.llun.thorfun;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import th.in.llun.thorfun.api.DefaultApiResponse;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import th.in.llun.thorfun.api.model.Neighbour;
import th.in.llun.thorfun.api.model.Story;
import th.in.llun.thorfun.api.model.ThorfunResult;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class StoryView extends SherlockFragmentActivity {

	public static final String KEY_STORY = "story";

	private Thorfun mThorfun = null;
	private CategoryStory mStory = null;
	private Menu mMenu = null;

	private ViewPager mViewPager;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_view);

		final View loadingView = (View) findViewById(R.id.story_view_loading);

		mThorfun = Thorfun.getInstance(this);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		setTitle(getString(R.string.story_title));

		Intent intent = getIntent();

		final String action = intent.getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			final List<String> segments = intent.getData().getPathSegments();

			final String dataUsername = segments.get(0);
			final String dataStory = segments.get(2);

			mThorfun.getNeightbour(dataUsername, new ThorfunResult<Neighbour>() {

				@Override
				public void onResponse(final Neighbour neighbour) {

					mThorfun.getStory(dataStory, new ThorfunResult<Story>() {

						@Override
						public void onResponse(Story response) {
							JSONObject object = response.getStory();
							try {
								object.put("neighbour", new JSONObject(neighbour.rawString()));
								mStory = new CategoryStory(object);

								renderDataWithStory();
								loadingView.setVisibility(View.GONE);
							} catch (JSONException e) {
								Log.e(Thorfun.LOG_TAG, "Can't put neighbour", e);
							}							
						}

					});
				}
			});

		} else {
			String json = intent.getStringExtra(KEY_STORY);
			try {
				mStory = new CategoryStory(new JSONObject(json));
				loadingView.setVisibility(View.GONE);
			} catch (JSONException e) {
				Log.e(Thorfun.LOG_TAG, "Can't parse JSON string", e);
			}

			renderDataWithStory();

		}

	}

	private void renderDataWithStory() {
		mSectionsPagerAdapter = new SectionsPagerAdapter(
		    getSupportFragmentManager(), mStory);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		if (mViewPager.getAdapter() == null) {
			mViewPager.setAdapter(mSectionsPagerAdapter);
		}

		mSectionsPagerAdapter.notifyDataSetChanged();

		TextView titleView = (TextView) findViewById(R.id.story_view_title);
		TextView username = (TextView) findViewById(R.id.story_view_username_text);
		TextView like = (TextView) findViewById(R.id.story_view_favorite_text);
		TextView time = (TextView) findViewById(R.id.story_view_time_text);

		applyFilterOverDrawable(username);
		applyFilterOverDrawable(like);
		applyFilterOverDrawable(time);

		titleView.setText(Html.fromHtml(mStory.getTitle()));
		username.setText(mStory.getNeightbour().getName());
		like.setText("" + mStory.getLikeNumber());
		time.setText(new PrettyTime().format(mStory.getTime()));

		Thorfun.getInstance(this).getStory(mStory.getID(),
		    new ThorfunResult<Story>() {

			    @Override
			    public void onResponse(Story response) {
				    if (response.isUserLiked()) {
					    MenuItem likeItem = mMenu.findItem(R.id.story_menu_like);
					    Drawable icon = likeItem.getIcon();
					    icon.setColorFilter(Color.RED, Mode.SRC_ATOP);
					    likeItem.setChecked(true);
				    }
			    }
		    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		if (mThorfun.isLoggedIn()) {
			inflater.inflate(R.menu.story_loggedin_menu, menu);

			MenuItem likeItem = menu.findItem(R.id.story_menu_like);
			Drawable drawable = likeItem.getIcon();
			drawable.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
			likeItem.setChecked(false);

		} else {
			inflater.inflate(R.menu.story_menu, menu);
		}

		MenuItem shareItem = menu.findItem(R.id.story_menu_share);
		Drawable drawable = shareItem.getIcon();
		drawable.setColorFilter(Color.WHITE, Mode.SRC_ATOP);

		mMenu = menu;

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
		case R.id.story_menu_like:
			if (item.isChecked()) {
				Drawable drawable = item.getIcon();
				drawable.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
				item.setChecked(false);

				mThorfun.unlike(mStory.getID(), new DefaultApiResponse<String>());
			} else {
				Drawable drawable = item.getIcon();
				drawable.setColorFilter(Color.RED, Mode.SRC_ATOP);
				item.setChecked(true);

				mThorfun.like(mStory.getID(), new DefaultApiResponse<String>());
			}

			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void applyFilterOverDrawable(TextView textView) {
		Drawable drawables[] = textView.getCompoundDrawables();
		for (Drawable drawable : drawables) {
			if (drawable != null) {
				drawable.setColorFilter(textView.getCurrentTextColor(), Mode.SRC_ATOP);
			}
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private CategoryStory mStory;

		public SectionsPagerAdapter(FragmentManager fm, CategoryStory story) {
			super(fm);
			mStory = story;
		}

		@Override
		public Fragment getItem(int position) {
			Bundle arguments = new Bundle();
			arguments.putString(KEY_STORY, mStory.rawString());

			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new StoryViewContentFragment();
				break;

			case 1:
				fragment = new StoryViewCommentFragment();
				break;
			}

			fragment.setArguments(arguments);
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.story_view_menu_content).toUpperCase(l);
			case 1:
				return getString(R.string.story_view_menu_comments).toUpperCase(l);
			}
			return null;
		}
	}
}
