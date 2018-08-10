package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

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
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie_;

import java.util.List;

public class MoviesAllFragment extends MoviesBaseFragment {

    private String currentFilterOption;
    private int currentSortByOption;
    private int currentSortDirectionOption;

    public MoviesAllFragment() {
        setHasOptionsMenu(true);
        setSearchFabVisibility(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        initFilteringAndSortingOptionsValues();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public List<Movie> requestMoviesFromDb() {
        switch (currentFilterOption) {
            case "all":
                return requestAllMovies();
            case "released_cinema":
                return requestMoviesReleasedInCinema();
            case "released_digital":
                return requestDigitalMoviesReleased();
            case "released_physical":
                return requestPhysicalMoviesReleased();
            case "not_released":
                return requestMoviesNotReleased();
            default:
                Log.e("MoviesAllFragment", "Filtering - displaying movies error");
                return null;
        }
    }

    private List<Movie> requestAllMovies() {
        return getMoviesBox().query()
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestMoviesReleasedInCinema() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.releaseDateInMilliseconds, todayInMilliseconds)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestDigitalMoviesReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.digitalReleaseDateInMilliseconds, todayInMilliseconds)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestPhysicalMoviesReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.physicalReleaseDateInMilliseconds, todayInMilliseconds)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestMoviesNotReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .greater(Movie_.releaseDateInMilliseconds, todayInMilliseconds)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private void initFilteringAndSortingOptionsValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_collection_filter),
                getString(R.string.settings_collection_filter_default)
        );
        currentSortByOption = sharedPrefs.getInt(
                getString(R.string.settings_collection_sort),
                MovieComparator.BY_DATE
        );

        currentSortDirectionOption = sharedPrefs.getInt(
                getString(R.string.settings_collection_sort_direction),
                MovieComparator.ASCENDING
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.collection_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
            case "released_digital":
                MenuItem digitalItem = menu.findItem(R.id.menu_show_digital);
                digitalItem.setEnabled(false);
                break;
            case "released_physical":
                MenuItem physicalItem = menu.findItem(R.id.menu_show_physical);
                physicalItem.setEnabled(false);
                break;
            case "not_released":
                MenuItem notReleasedItem = menu.findItem(R.id.menu_show_not_released);
                notReleasedItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Filtering - setting menu error");
                break;
        }

        // Set active option invisible
        switch (currentSortByOption) {
            case MovieComparator.BY_DATE:
                MenuItem dateItem = menu.findItem(R.id.menu_sort_by_date);
                dateItem.setEnabled(false);
                break;
            case MovieComparator.BY_RATING:
                MenuItem ratingItem = menu.findItem(R.id.menu_sort_by_rating);
                ratingItem.setEnabled(false);
                break;
            default:
                Log.e("MoviesAllFragment", "Sorting error");
                break;
        }

        // Set active option invisible
        switch (currentSortDirectionOption) {
            case MovieComparator.ASCENDING:
                MenuItem ascItem = menu.findItem(R.id.menu_sort_asc);
                ascItem.setEnabled(false);
                break;
            case MovieComparator.DESCENDING:
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
            case R.id.menu_show_cinema:
                saveSettingFilterCinema();
                break;
            case R.id.menu_show_digital:
                saveSettingFilterDigital();
                break;
            case R.id.menu_show_physical:
                saveSettingFilterPhysical();
                break;
            case R.id.menu_show_not_released:
                saveSettingFilterNotReleased();
                break;
            case R.id.menu_sort_by_date:
                saveSettingSortBy(MovieComparator.BY_DATE);
                break;
            case R.id.menu_sort_by_rating:
                saveSettingSortBy(MovieComparator.BY_RATING);
                break;
            case R.id.menu_sort_asc:
                saveSettingSortDirection(MovieComparator.ASCENDING);
                break;
            case R.id.menu_sort_desc:
                saveSettingSortDirection(MovieComparator.DESCENDING);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshMovieList();
        return true;
    }

    // Save in SharedPreferences: show all movies
    private void saveSettingFilterAll() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_collection_filter), "all");
        editor.apply();
    }

    // Save in SharedPreferences: show movies released in cinema
    private void saveSettingFilterCinema() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_collection_filter), "released_cinema");
        editor.apply();
    }

    // Save in SharedPreferences: show digital movies released
    private void saveSettingFilterDigital() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_collection_filter), "released_digital");
        editor.apply();
    }

    // Save in SharedPreferences: show physical movies released
    private void saveSettingFilterPhysical() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_collection_filter), "released_physical");
        editor.apply();
    }

    // Save in SharedPreferences: show movies not released
    private void saveSettingFilterNotReleased() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_collection_filter), "not_released");
        editor.apply();
    }

    private void saveSettingSortBy(int sortBy) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_collection_sort), sortBy);
        editor.apply();
    }

    private void saveSettingSortDirection(int sortDirection) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_collection_sort_direction), sortDirection);
        editor.apply();
    }

}
