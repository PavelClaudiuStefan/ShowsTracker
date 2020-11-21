package com.pavelclaudiustefan.shadowapps.showstracker.ui.auth;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.pavelclaudiustefan.shadowapps.showstracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    public static final String TAG = "ShadowDebug";

    private static final int LOGIN_PAGE = 0;
    private static final int SIGNUP_PAGE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle("Log In");

        setUpViewPager();
    }

    private void setUpViewPager() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            String[] pageTitles = {"Log In", "Sign Up"};
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                hideKeyboard();
            }

            @Override
            public void onPageSelected(int i) { toolbar.setTitle(pageTitles[i]); }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case LOGIN_PAGE:
                    return new LoginFragment();
                case SIGNUP_PAGE:
                    return new SignupFragment();
                default:
                    Log.e("ShadowDebug", "AuthActivity - SectionsPagerAdapter - getItem(position): Error", new Exception("Invalid position value in pager adapter"));
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void changeToSection(int page) {
        viewPager.setCurrentItem(page, true);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
