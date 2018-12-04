package com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.TvShowsCategoryAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.base.ShowsSectionsContainerActivity;

import butterknife.BindView;

import static com.pavelclaudiustefan.shadowapps.showstracker.utils.Utils.isFreeAccount;

public class TvShowsActivity extends ShowsSectionsContainerActivity {

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.shows_ad_container)
    RelativeLayout showsAdContainer;

    public TvShowsActivity() {
        setLayout(R.layout.activity_shows);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("TV Shows");
        navigationView.getMenu().findItem(R.id.nav_tv_shows).setChecked(true);

        if (shouldShowAds()) {
            setUpAds();
        }
    }

    @Override
    public FragmentStatePagerAdapter getFragmentStatePagerAdapter() {
        return new TvShowsCategoryAdapter(this, getSupportFragmentManager());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_tv_shows) {
            closeDrawer();
            return true;
        } else {
            return super.onNavigationItemSelected(item);
        }
    }

    private boolean shouldShowAds() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String email = firebaseUser != null ? firebaseUser.getEmail() : "";

        return isFreeAccount(email);
    }

    private void setUpAds() {
        // TODO - Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        // Creates adView and adds it to a relative layout
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        adView.setLayoutParams(params);
        showsAdContainer.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
