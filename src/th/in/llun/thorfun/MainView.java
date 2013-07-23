package th.in.llun.thorfun;

import java.util.Locale;

import th.in.llun.thorfun.api.ApiResponse;
import th.in.llun.thorfun.api.Thorfun;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;

public class MainView extends FragmentActivity {

	public static final String PAGE_MAIN = "main";
	public static final String PAGE_LOGIN = "login";
	public static final String PAGE_MAIN_LOGGEDIN = "main_loggedin";

	private String page;
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
		    getSupportFragmentManager());

		showMain();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (page == PAGE_MAIN) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.activity_main_menu, menu);
			return true;
		} else if (page == PAGE_MAIN_LOGGEDIN) {
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
			Thorfun.getInstance(this).logout();
			invalidateOptionsMenu();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showLogin() {
		setContentView(R.layout.activity_main_view_login);

		page = PAGE_LOGIN;
		invalidateOptionsMenu();

		final Activity mActivity = this;
		Button mCancelButton = (Button) findViewById(R.id.login_cancel_button);
		mCancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				showMain();
			}
		});

		final EditText mUsername = (EditText) findViewById(R.id.login_username_field);
		final EditText mPassword = (EditText) findViewById(R.id.login_password_field);
		Button mLoginButton = (Button) findViewById(R.id.login_submit_button);
		mLoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Thorfun.getInstance(mActivity).login(mUsername.getText().toString(),
				    mPassword.getText().toString(), new ApiResponse<String>() {

					    @Override
					    public void onResponse(String result) {
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

	public void showMain() {
		setContentView(R.layout.activity_main_view);

		page = PAGE_MAIN;
		invalidateOptionsMenu();

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		if (mViewPager.getAdapter() == null) {
			mViewPager.setAdapter(mSectionsPagerAdapter);
		}

		mSectionsPagerAdapter.notifyDataSetChanged();

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new StoryFragment();
				break;

			case 1:
				fragment = new BoardFragment();
				break;
			}

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
				return getString(R.string.story_title).toUpperCase(l);
			case 1:
				return getString(R.string.board_title).toUpperCase(l);
			}
			return null;
		}
	}

}
