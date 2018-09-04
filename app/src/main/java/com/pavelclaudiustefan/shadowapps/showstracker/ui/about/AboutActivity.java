package com.pavelclaudiustefan.shadowapps.showstracker.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pavelclaudiustefan.shadowapps.showstracker.BuildConfig;
import com.pavelclaudiustefan.shadowapps.showstracker.R;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpAboutView();
        setupActionBar();
        setTitle("About");

    }

    private void setUpAboutView() {
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher_sloth)
                .setDescription("Shows tracker is an app used to track your favorite movies and TV shows that you watched or plan on watching. You can also create or join groups, where you can see the movies or TV shows that every member wants to watch, so you can enjoy them toghether.")
                .addItem(getVersionElement())
                .addEmail("claudiu.shadow.apps@gmail.com", "Contact email")
                .addGitHub("pavelclaudiustefan", "GitHub")
                .addItem(getLibrariesElement())
                .addItem(getTmdbElement())
                .addItem(getCopyrightsElement())
                .create();

        setContentView(aboutPage);
    }

    private Element getVersionElement() {
        String versionName = BuildConfig.VERSION_NAME;
        return new Element("Version " + versionName, R.drawable.ic_android);
    }

    private Element getCopyrightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconDrawable(R.drawable.ic_copyright);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        //copyRightsElement.setGravity(Gravity.CENTER);
        copyRightsElement.setOnClickListener(v -> Toast.makeText(AboutActivity.this, copyrights, Toast.LENGTH_SHORT).show());
        return copyRightsElement;
    }

    private Element getLibrariesElement() {
        Element librariesElement = new Element("Community libraries", R.drawable.ic_public);
        librariesElement.setIntent(new Intent(this, LibrariesActivity.class));
        return librariesElement;
    }

    private Element getTmdbElement() {
        Element tmdbElement = new Element("The Movie Database", R.drawable.ic_tmdb);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://www.themoviedb.org/"));
        tmdbElement.setIntent(i);
        return tmdbElement;
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Finishes the activity if the Up button is pressed
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
