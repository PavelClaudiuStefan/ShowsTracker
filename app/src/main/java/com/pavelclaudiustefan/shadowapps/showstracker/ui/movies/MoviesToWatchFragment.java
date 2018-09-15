package com.pavelclaudiustefan.shadowapps.showstracker.ui.movies;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Movie_;

import java.util.List;

public class MoviesToWatchFragment extends MoviesBaseFragment {

    private String currentFilterOption;
    private int currentSortByOption;
    private int currentSortDirectionOption;

    @Override
    public void initFilteringAndSortingOptionsValues() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        currentFilterOption = sharedPrefs.getString(
                getString(R.string.settings_movies_to_watch_filter),
                getString(R.string.settings_movies_to_watch_filter_default)
        );
        currentSortByOption = sharedPrefs.getInt(
                getString(R.string.settings_movies_to_watch_sort_by),
                MovieComparator.BY_DATE
        );
        currentSortDirectionOption = sharedPrefs.getInt(
                getString(R.string.settings_movies_to_watch_sort_direction),
                MovieComparator.ASCENDING
        );
    }

    @Override
    public List<Movie> requestMoviesFromDb() {
        switch (currentFilterOption) {
            case "released_cinema":
                return requestMoviesReleasedInCinema();
            case "released_digital":
                return requestDigitalMoviesReleased();
            case "released_physical":
                return requestPhysicalMoviesReleased();
            case "upcoming":
                return requestUpcomingMovies();
            default:
                Log.e("ShadowDebug", "Filtering - displaying movies error");
                return null;
        }
    }

    private List<Movie> requestMoviesReleasedInCinema() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.releaseDateInMilliseconds, todayInMilliseconds)
                .equal(Movie_.watched, false)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestDigitalMoviesReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.digitalReleaseDateInMilliseconds, todayInMilliseconds)
                .equal(Movie_.watched, false)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestPhysicalMoviesReleased() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .less(Movie_.physicalReleaseDateInMilliseconds, todayInMilliseconds)
                .equal(Movie_.watched, false)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    private List<Movie> requestUpcomingMovies() {
        long todayInMilliseconds = System.currentTimeMillis();

        return getMoviesBox().query()
                .greater(Movie_.releaseDateInMilliseconds, todayInMilliseconds)
                .equal(Movie_.watched, false)
                .sort(new MovieComparator(currentSortByOption, currentSortDirectionOption))
                .build()
                .find();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.movies_to_watch_menu,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Set active option invisible
        switch (currentFilterOption) {
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
            case "upcoming":
                MenuItem upcomingItem = menu.findItem(R.id.menu_show_upcoming);
                upcomingItem.setEnabled(false);
                break;
            default:
                Log.e("ShadowDebug", "Filtering - setting menu error");
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
            case MovieComparator.ALPHABETICALLY:
                MenuItem alphabeticallyItem = menu.findItem(R.id.menu_sort_alphabetically);
                alphabeticallyItem.setEnabled(false);
            default:
                Log.e("ShadowDebug", "Sorting error");
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
                Log.e("ShadowDebug", "Sorting direction error");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_show_cinema:
                saveFilterOption("released_cinema");
                break;
            case R.id.menu_show_digital:
                saveFilterOption("released_digital");
                break;
            case R.id.menu_show_physical:
                saveFilterOption("released_physical");
                break;
            case R.id.menu_show_upcoming:
                saveFilterOption("upcoming");
                break;
            case R.id.menu_sort_by_date:
                saveSortByOption(MovieComparator.BY_DATE);
                break;
            case R.id.menu_sort_by_rating:
                saveSortByOption(MovieComparator.BY_RATING);
                break;
            case R.id.menu_sort_alphabetically:
                saveSortByOption(MovieComparator.ALPHABETICALLY);
                break;
            case R.id.menu_sort_asc:
                saveSortDirectionOption(MovieComparator.ASCENDING);
                break;
            case R.id.menu_sort_desc:
                saveSortDirectionOption(MovieComparator.DESCENDING);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        refreshMovieList();
        return true;
    }

    private void saveFilterOption(String filterOption) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.settings_movies_to_watch_filter), filterOption);
        editor.apply();
    }

    private void saveSortByOption(int sortBy) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_movies_to_watch_sort_by), sortBy);
        editor.apply();
    }

    private void saveSortDirectionOption(int sortDirection) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(getString(R.string.settings_movies_to_watch_sort_direction), sortDirection);
        editor.apply();
    }

}
