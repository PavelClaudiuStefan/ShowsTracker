package com.pavelclaudiustefan.shadowapps.showstracker.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.data.MovieContract;

public class MoviesAllFragment extends MoviesFragment {

    public MoviesAllFragment() {
        setHasOptionsMenu(true);
        setSearchFabVisibility(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_to_watch_filter),
                getString(R.string.settings_to_watch_filter_default)
        );

        switch (currentFilterOption) {
            case "all":
                displayAllMovies();
                break;
            case "released_cinema":
                displayMoviesReleasedInCinema();
                break;
            case "released_dvd":
                displayMoviesReleasedOnDvd();
                break;
            case "not_released":
                displayMoviesNotReleased();
            default:
                Log.e("MoviesAllFragment", "Filtering error");
                break;
        }

        setMovieOrder();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.collection_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Get active option, or default option if active doesn't exist
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_to_watch_filter),
                getString(R.string.settings_to_watch_filter_default)
        );
        String currentSortOption = sharedPrefs.getString(
                getString(R.string.settings_to_watch_sort),
                getString(R.string.settings_to_watch_sort_default)
        );

        String currentSortDirectionOption = sharedPrefs.getString(
                getString(R.string.settings_to_watch_sort_direction),
                getString(R.string.settings_to_watch_sort_direction_default)
        );

        // Set active option invisible
        switch (currentFilterOption) {
            case "all":
                MenuItem allItem = menu.findItem(R.id.menu_show_all);
                allItem.setEnabled(false);
                break;
            case "released_cinema":
                MenuItem cinemaItem = menu.findItem(R.id.menu_show_cinema);
                cinemaItem.setEnabled(false);
                break;
            case "released_dvd":
                MenuItem dvdItem = menu.findItem(R.id.menu_show_dvd);
                dvdItem.setEnabled(false);
                break;
            case "not_released":
                MenuItem notReleasedItem = menu.findItem(R.id.menu_show_not_released);
                notReleasedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Filtering error");
                break;
        }

        // Set active option invisible
        switch (currentSortOption) {
            case "release_date":
                MenuItem dateItem = menu.findItem(R.id.menu_sort_by_date);
                dateItem.setEnabled(false);
                break;
            case "average_vote":
                MenuItem ratingItem = menu.findItem(R.id.menu_sort_by_rating);
                ratingItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Sorting error");
                break;
        }

        // Set active option invisible
        switch (currentSortDirectionOption) {
            case "ASC":
                MenuItem ascItem = menu.findItem(R.id.menu_sort_asc);
                ascItem.setEnabled(false);
                break;
            case ("DESC"):
                MenuItem descItem = menu.findItem(R.id.menu_sort_desc);
                descItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Sorting direction error");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_all:
                saveSettingFilterAll();
                break;
            case R.id.menu_show_dvd:
                saveSettingFilterDvd();
                break;
            case R.id.menu_show_cinema:
                saveSettingFilterCinema();
                break;
            case R.id.menu_show_not_released:
                saveSettingFilterNotReleased();
                break;
            case R.id.menu_sort_by_date:
                saveSettingSortBy(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS);
                break;
            case R.id.menu_sort_by_rating:
                saveSettingSortBy(MovieContract.MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
                break;
            case R.id.menu_sort_asc:
                saveSettingSortDirection("ASC");
                break;
            case R.id.menu_sort_desc:
                saveSettingSortDirection("DESC");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshMovieList();
        return true;
    }

    private void displayAllMovies() {
        setSelection(null);
        setSelectionArgs(null);
    }

    // TODO
    private void displayMoviesReleasedOnDvd() {
        // Show movies that have not been watched, released on dvd, in cinema, or not released
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + ">?";

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};

        setSelection(selection);
        setSelectionArgs(selectionArgs);
    }

    private void displayMoviesReleasedInCinema() {
        // Show movies that have not been watched, released on dvd, in cinema, or not released
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + "<?";

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};

        setSelection(selection);
        setSelectionArgs(selectionArgs);
    }

    private void displayMoviesNotReleased() {
        // Show movies that have not been watched, released on dvd, in cinema, or not released
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE_IN_MILLISECONDS + ">?";

        long todayInMilliseconds = System.currentTimeMillis();
        String[] selectionArgs = {String.valueOf(todayInMilliseconds)};

        setSelection(selection);
        setSelectionArgs(selectionArgs);
    }

    private void setMovieOrder() {
        // Order movies by date, or rating
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPrefs.getString(
                getString(R.string.settings_to_watch_sort),
                getString(R.string.settings_to_watch_sort_default)
        );

        String sortDirection = sharedPrefs.getString(
                getString(R.string.settings_to_watch_sort_direction),
                getString(R.string.settings_to_watch_sort_direction_default)
        );

        String sortOrder = sortBy + " " + sortDirection;

        setSortOrder(sortOrder);
    }

    // Save in SharedPreferences: show all movies
    private void saveSettingFilterAll() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_filter), "all");
        editor.apply();
    }

    // Save in SharedPreferences: show movies released on dvd
    private void saveSettingFilterDvd() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_filter), "released_dvd");
        editor.apply();
    }

    // Save in SharedPreferences: show movies released in cinema
    private void saveSettingFilterCinema() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_filter), "released_cinema");
        editor.apply();
    }

    // Save in SharedPreferences: show movies not released
    private void saveSettingFilterNotReleased() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_filter), "not_released");
        editor.apply();
    }

    private void saveSettingSortBy(String sortBy) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_sort), sortBy);
        editor.apply();
    }

    private void saveSettingSortDirection(String sortDirection) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_to_watch_sort_direction), sortDirection);
        editor.apply();
    }

}
