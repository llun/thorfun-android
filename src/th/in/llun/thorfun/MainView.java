package th.in.llun.thorfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import th.in.llun.thorfun.api.ApiResponse;
import th.in.llun.thorfun.api.Thorfun;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class MainView extends FragmentActivity {

	public static final String PAGE_MAIN = "main";
	public static final String PAGE_LOGIN = "login";
	public static final String PAGE_MAIN_LOGGEDIN = "main_loggedin";

	private String mPage;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
		    getSupportFragmentManager(), Thorfun.getInstance(this));

		showMain();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mPage == PAGE_MAIN) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.activity_main_menu, menu);
			return true;
		} else if (mPage == PAGE_MAIN_LOGGEDIN) {
			MenuInflater inflater = getMenuInflater();
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
		invalidateOptionsMenu();

		final Activity activity = this;
		Button cancelButton = (Button) findViewById(R.id.login_cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				InputMethodManager inputManager = (InputMethodManager) activity
				    .getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(activity.getCurrentFocus()
				    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				showMain();
			}
		});

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
						    InputMethodManager inputManager = (InputMethodManager) activity
						        .getSystemService(Context.INPUT_METHOD_SERVICE);
						    inputManager.hideSoftInputFromWindow(activity.getCurrentFocus()
						        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
						    showMain();
					    }

					    @Override
					    public void onError(Exception exception) {
						    Log.v(Thorfun.LOG_TAG, exception.getMessage(), exception);
						    // Show login fail?
					    }
				    });
			}
		});
	}

	private void showMain() {
		setContentView(R.layout.activity_main_view);

		if (Thorfun.getInstance(this).isLoggedIn()) {
			mPage = PAGE_MAIN_LOGGEDIN;
		} else {
			mPage = PAGE_MAIN;
		}

		invalidateOptionsMenu();
		mSectionsPagerAdapter.update();

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		if (mViewPager.getAdapter() == null) {
			mViewPager.setAdapter(mSectionsPagerAdapter);
		}

		mSectionsPagerAdapter.notifyDataSetChanged();

	}

	private void logout() {
		Thorfun.getInstance(this).logout(new ApiResponse<String>() {

			@Override
			public void onResponse(String result) {
				mPage = PAGE_MAIN;
				invalidateOptionsMenu();
			}

			@Override
			public void onError(Exception exception) {
				// Handler error
			}
		});
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String KEY_TITLE = "title";
		private static final String KEY_FRAGMENT = "fragment";

		private Thorfun mThorfun;
		private ArrayList<HashMap<String, Object>> mOptions = new ArrayList<HashMap<String, Object>>(
		    3);

		public SectionsPagerAdapter(FragmentManager fm, Thorfun thorfun) {
			super(fm);
			mThorfun = thorfun;
		}

		public void update() {
			Locale l = Locale.getDefault();

			mOptions.clear();
			HashMap<String, Object> myStory = new HashMap<String, Object>();
			myStory.put(KEY_TITLE, getString(R.string.my_story_title).toUpperCase(l));
			myStory.put(KEY_FRAGMENT, new MyStoryFragment());

			HashMap<String, Object> story = new HashMap<String, Object>();
			story.put(KEY_TITLE, getString(R.string.story_title).toUpperCase(l));
			story.put(KEY_FRAGMENT, new StoryFragment());

			HashMap<String, Object> board = new HashMap<String, Object>();
			board.put(KEY_TITLE, getString(R.string.board_title).toUpperCase(l));
			board.put(KEY_FRAGMENT, new BoardFragment());

			if (mThorfun.isLoggedIn()) {
				mOptions.add(myStory);
			}
			mOptions.add(story);
			mOptions.add(board);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = (Fragment) mOptions.get(position).get(KEY_FRAGMENT);
			return fragment;
		}

		@Override
		public int getCount() {
			return mOptions.size();
		}

		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return (String) mOptions.get(position).get(KEY_TITLE);
		}
	}

}
