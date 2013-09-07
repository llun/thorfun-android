package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import th.in.llun.thorfun.api.ApiResponse;
import th.in.llun.thorfun.api.Thorfun;
import th.in.llun.thorfun.api.model.CategoryStory;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainView extends SherlockFragmentActivity {

	public static final String PAGE_MAIN = "main";
	public static final String PAGE_LOGIN = "login";
	public static final String PAGE_MAIN_LOGGEDIN = "main_loggedin";

	private String mPage;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		showMain();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mPage == PAGE_MAIN) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.activity_main_menu, menu);
			return true;
		} else if (mPage == PAGE_MAIN_LOGGEDIN) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.activity_loggedin_menu, menu);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.main_menu_login:
			showLogin();
			return true;
		case R.id.main_menu_logout:
			logout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showLogin() {
		setContentView(R.layout.activity_main_view_login);

		mPage = PAGE_LOGIN;
		supportInvalidateOptionsMenu();

		final SherlockFragmentActivity activity = this;

		final EditText username = (EditText) findViewById(R.id.login_username_field);
		final EditText password = (EditText) findViewById(R.id.login_password_field);
		Button loginButton = (Button) findViewById(R.id.login_submit_button);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Thorfun.getInstance(activity).login(username.getText().toString(),
				    password.getText().toString(), new ApiResponse<String>() {

					    @Override
					    public void onResponse(String result) {
						    try {
							    InputMethodManager inputManager = (InputMethodManager) activity
							        .getSystemService(Context.INPUT_METHOD_SERVICE);
							    inputManager.hideSoftInputFromWindow(activity
							        .getCurrentFocus().getWindowToken(),
							        InputMethodManager.HIDE_NOT_ALWAYS);
						    } catch (Exception e) {
							    Log.e(Thorfun.LOG_TAG, "Can't hide keybaord", e);
						    }
						    View errorMessage = activity
						        .findViewById(R.id.login_error_message);
						    if (result.equals("false")) {
							    errorMessage.setVisibility(View.VISIBLE);
						    } else {
							    errorMessage.setVisibility(View.GONE);
							    showMain();
						    }
					    }

					    @Override
					    public void onError(Exception exception) {
						    Log.v(Thorfun.LOG_TAG, exception.getMessage(), exception);
						    // Show login fail?
					    }
				    });
			}
		});
		username.requestFocus();
	}

	private void showMain() {
		setContentView(R.layout.activity_main_view);

		if (Thorfun.getInstance(this).isLoggedIn()) {
			mPage = PAGE_MAIN_LOGGEDIN;
		} else {
			mPage = PAGE_MAIN;
		}

		supportInvalidateOptionsMenu();

		// Set up the ViewPager with the sections adapter.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
		    getSupportFragmentManager(), Thorfun.getInstance(this));

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(sectionsPagerAdapter);
		sectionsPagerAdapter.notifyDataSetChanged();

	}

	private void logout() {
		final Activity self = this;
		Thorfun.getInstance(this).logout(new ApiResponse<String>() {

			@Override
			public void onResponse(String result) {
				mPage = PAGE_MAIN;
				supportInvalidateOptionsMenu();

				// Set up the ViewPager with the sections adapter.
				SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(
				    getSupportFragmentManager(), Thorfun.getInstance(self));

				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(sectionsPagerAdapter);
				sectionsPagerAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(Exception exception) {
				// Handler error
			}
		});
	}

	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		private static final String KEY_TITLE = "title";
		private static final String KEY_FRAGMENT = "fragment";

		private Thorfun mThorfun;

		private ArrayList<HashMap<String, Object>> mOptions = new ArrayList<HashMap<String, Object>>(
		    3);

		public SectionsPagerAdapter(FragmentManager fm, Thorfun thorfun) {
			super(fm);
			mThorfun = thorfun;

			Locale l = Locale.getDefault();

			mOptions.clear();
			MyStoryFragment myStoriesFragment = new MyStoryFragment();
			HashMap<String, Object> myStories = new HashMap<String, Object>();
			myStories.put(KEY_TITLE, getString(R.string.my_story_title)
			    .toUpperCase(l));
			myStories.put(KEY_FRAGMENT, myStoriesFragment);

			Bundle topStoriesArgument = new Bundle();
			topStoriesArgument.putString(StoryFragment.ARGUMENT_SORTBY,
			    CategoryStory.SORT_HOT);
			StoryFragment topStoriesFragment = new StoryFragment();
			topStoriesFragment.setArguments(topStoriesArgument);
			HashMap<String, Object> topStories = new HashMap<String, Object>();
			topStories.put(KEY_TITLE, getString(R.string.top_story_title)
			    .toUpperCase(l));
			topStories.put(KEY_FRAGMENT, topStoriesFragment);

			Bundle storiesArgument = new Bundle();
			storiesArgument.putString(StoryFragment.ARGUMENT_SORTBY,
			    CategoryStory.SORT_TIME);
			StoryFragment storiesFragment = new StoryFragment();
			storiesFragment.setArguments(storiesArgument);
			HashMap<String, Object> stories = new HashMap<String, Object>();
			stories.put(KEY_TITLE, getString(R.string.story_title).toUpperCase(l));
			stories.put(KEY_FRAGMENT, storiesFragment);

			BoardFragment boardFragment = new BoardFragment();
			HashMap<String, Object> board = new HashMap<String, Object>();
			board.put(KEY_TITLE, getString(R.string.board_title).toUpperCase(l));
			board.put(KEY_FRAGMENT, boardFragment);

			if (mThorfun.isLoggedIn()) {
				mOptions.add(myStories);
				mOptions.add(stories);
			}
			mOptions.add(topStories);
			mOptions.add(board);
		}

		public void update() {

		}

		@Override
		public Fragment getItem(int position) {
			Log.d(Thorfun.LOG_TAG, "Get item: " + position);
			return (Fragment) mOptions.get(position).get(KEY_FRAGMENT);
		}

		@Override
		public int getCount() {
			return mOptions.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return (String) mOptions.get(position).get(KEY_TITLE);
		}
	}

}
