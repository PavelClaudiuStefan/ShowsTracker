package com.pavelclaudiustefan.shadowapps.showstracker.ui.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.GoogleApiManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.about.AboutActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.auth.AuthActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.groups.GroupsActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MoviesActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.settings.SettingsActivity;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.tvshows.TvShowsActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "BaseActivity";

    private int layout;

    private FirebaseAuth firebaseAuth;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private TextView userDisplayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        ButterKnife.bind(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Set logged user display name (Username or email)
        View headerView = navigationView.getHeaderView(0);
        userDisplayTextView = headerView.findViewById(R.id.display_name);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            updateDrawerUserDisplayName(currentUser.getDisplayName());
        }

        initSharedPreferencesListener();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    private void updateDrawerUserDisplayName(String displayName) {
        if (displayName != null && !displayName.isEmpty()) {
            userDisplayTextView.setText(displayName);
        } else {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                String email = currentUser.getEmail();
                userDisplayTextView.setText(email);
            } else {
                userDisplayTextView.setText(R.string.not_logged_in);
            }
        }
    }

    private void initSharedPreferencesListener() {
        listener = (prefs, key) -> {
            if (key.equals("display_name")) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    String newDisplayName = prefs.getString(key, currentUser.getDisplayName());
                    currentUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newDisplayName).build());
                    updateDrawerUserDisplayName(newDisplayName);
                    updateUsersDb(newDisplayName);
                }
            }
        };
    }

    private void updateUsersDb(String displayName) {
        if (firebaseAuth.getUid() != null) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String, Object> user = new HashMap<>();
            user.put("displayName", displayName);
            user.put("updatedAt", Timestamp.now());
            firestore.collection("users")
                    .document(firebaseAuth.getUid())
                    .update(user)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User updated"))
                    .addOnFailureListener(error -> Log.e(TAG, "Error updating user", error));
        } else {
            Log.e(TAG, "fireBaseAuth.getUid is null");
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shows, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //if (id == R.id.nav_main) {
            //startActivity(new Intent(this, MainActivity.class));
            //finish();
        //} else
        if (id == R.id.nav_movies) {
            startActivity(new Intent(this, MoviesActivity.class));
            finish();
        } else if (id == R.id.nav_tv_shows) {
            startActivity(new Intent(this, TvShowsActivity.class));
            finish();
        } else if (id == R.id.nav_groups) {
            startActivity(new Intent(this, GroupsActivity.class));
            finish();
        } else if (id == R.id.nav_settings) {
            item.setCheckable(false);
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_about) {
            item.setCheckable(false);
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.logout) {
            item.setCheckable(false);
            signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        }

        closeDrawer();
        return true;
    }

    public void closeDrawer() {
        drawer.closeDrawer(GravityCompat.START);
    }

    private void signOut() {
        firebaseAuth.signOut();

        // Google signout
        GoogleSignInClient client = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        client.signOut();

        // Facebook signout
        LoginManager.getInstance().logOut();
    }
}
