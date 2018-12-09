package com.pavelclaudiustefan.shadowapps.showstracker.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowsCardsAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.comparators.MovieComparator;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.movies.MovieActivityDb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class GroupActivity extends AppCompatActivity {

    public static final String TAG = "GroupActivity";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private String groupTitle;

    private ArrayList<Movie> movies;
    private ShowsCardsAdapter<Movie> moviesListAdapter;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private Box<Movie> moviesBox;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setupActionBar();
        ButterKnife.bind(this);

        Intent intent = getIntent();
        groupTitle = intent.getStringExtra("group_title");
        setTitle(groupTitle);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

        setIsOwner();

        //initFilteringAndSortingOptionsValues();
        requestGroupsAndAddToAdapter();
        setUpRecyclerView();
    }

    private void requestGroupsAndAddToAdapter() {
        movies = new ArrayList<>();
        requestMoviesAndAddSnapshotListener();
        moviesListAdapter = new ShowsCardsAdapter<>(this, movies, R.menu.menu_movies_list, new ShowsCardsAdapter.ShowsAdapterListener() {
            @Override
            public void onAddRemoveSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(GroupActivity.this, "Add/Remove button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onWatchUnwatchSelected(int position, MenuItem menuItem) {
                // TODO
                Toast.makeText(GroupActivity.this, "Watch/Unwatch button pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCardSelected(int position, CardView cardView) {
                Intent intent = new Intent(GroupActivity.this, MovieActivityDb.class);
                Movie movie = movies.get(position);
                intent.putExtra("tmdb_id", movie.getTmdbId());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(GroupActivity.this, cardView.findViewById(R.id.image), "image");
                startActivity(intent, options.toBundle());
            }

            @Override
            public boolean onLongClicked(int position, CardView cardView) {
                return false;
            }
        });
    }

    // Realtime updates
    private void requestMoviesAndAddSnapshotListener() {
        if (firebaseAuth.getUid() != null) {
            DocumentReference groupReference = firestore.collection("groups").document(groupTitle);

            groupReference.collection("movies")
                    .addSnapshotListener(this, (moviesSnapshots, error) -> {
                        if (error != null) {
                            Log.e(TAG, "snapshotListener failure", error);
                            return;
                        }
                        if (moviesSnapshots != null && !moviesSnapshots.isEmpty()) {

                            groupReference.collection("users")
                                    .get()
                                    .addOnSuccessListener(this, usersSnapshots -> {
                                        movies.clear();
                                        ArrayList<Long> tmdbIds = new ArrayList<>();
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : moviesSnapshots) {
                                            Long nrOfUsers = queryDocumentSnapshot.getLong("nrOfUsers");
                                            if (nrOfUsers != null && usersSnapshots.size() == nrOfUsers) {
                                                tmdbIds.add(queryDocumentSnapshot.getLong("tmdbId"));
                                            }
                                        }
                                        if (!tmdbIds.isEmpty()) {
                                            movies.addAll(getMoviesFromDb(tmdbIds));
                                            Collections.sort(movies, new MovieComparator(MovieComparator.BY_RATING, MovieComparator.DESCENDING));
                                            moviesListAdapter.notifyDataSetChanged();
                                        }
                                        loadingIndicator.setVisibility(View.GONE);

                                    })
                                    .addOnFailureListener(e -> {
                                        setUpEmptyView(R.string.connection_issues);
                                        Log.e(TAG, "Get users array size failure: ", e);
                                    });

                        } else {
                            setUpEmptyView(R.string.no_movies_added);
                            Log.i(TAG, "requestMoviesAndAddSnapshotListener - Current data null or empty");
                        }
                    });
        } else {
            Log.e(TAG, "FirebaseAuth.getUid() == null");
        }
    }

    private List<Movie> getMoviesFromDb(ArrayList<Long> tmdbIds) {
        return moviesBox.get(tmdbIds);
    }

    private void setUpEmptyView(int resid) {
        //Only visible if no movies are found
        //emptyStateTextView.setText(R.string.no_movies_added);
        if (movies == null || movies.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(resid);
        }

        loadingIndicator.setVisibility(View.GONE);
    }

    private void setUpRecyclerView() {
        recyclerView.setAdapter(moviesListAdapter);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(moviesListAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);
        });
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
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        if (id == R.id.action_exit_group) {
            if (isOwner) {
                deleteGroup();
            } else {
                exitGroup();
            }
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitGroup() {
        if (firebaseAuth.getUid() != null) {
            // Delete group from root/users/{userId}/groups/{someGroupId}/groupDocument
            CollectionReference userGroupsReference = firestore.collection("users").document(firebaseAuth.getUid()).collection("groups");
            userGroupsReference
                    .whereEqualTo("title", groupTitle)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            userGroupsReference.document(documentSnapshot.getId())
                                    .delete()
                                    .addOnFailureListener(e -> Log.e(TAG, "Exit group - Failure deleting group", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Exit group - Failure getting group generatedId", e));

            // Delete user from root/groups/{groupTitle}/users/{someUserId}/userDocument
            CollectionReference groupUsersReference = firestore.collection("groups").document(groupTitle).collection("users");
            groupUsersReference
                    .whereEqualTo("userId",  firebaseAuth.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            groupUsersReference.document(documentSnapshot.getId())
                                    .delete()
                                    .addOnFailureListener(e -> Log.e(TAG, "Exit group - Failure deleting user", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Delete user from group failure: ", e));

            List<Movie> allMovies = moviesBox.getAll();
            for (Movie movie : allMovies) {
                if (!movie.isWatched()) {
                    // Valid movie is removed -> lower the number of users that have this movie as valid (valid = unwatched collection movie)
                    updateGroupMovie(movie.getTmdbId(), -1);
                } else {
                    // Invalid movie is removed -> Doesn't affect number of valid movies
                    updateGroupMovie(movie.getTmdbId(), 0);
                }
            }
        }
    }

    // If movie from originUser is on every group member's to watch list -> Add to group movies
    // Add or update movie data in group/{groupTitle}/movies
    private void updateGroupMovie(long tmdbId, int increment) {
        CollectionReference groupMovies = firestore.collection("groups").document(groupTitle).collection("movies");
        groupMovies.document(String.valueOf(tmdbId))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> updatedMovieData = new HashMap<>();
                    updatedMovieData.put("tmdbId", tmdbId);
                    if (documentSnapshot.exists()) {
                        Long nrOfUsers = documentSnapshot.getLong("nrOfUsers");
                        if (nrOfUsers != null) {
                            updatedMovieData.put("nrOfUsers", nrOfUsers + increment);
                            groupMovies.document(String.valueOf(tmdbId))
                                    .set(updatedMovieData)
                                    .addOnFailureListener(e -> Log.e(TAG, "Updating movies (incrementing nrOfUsers) in groups/{title}/movies failure: ", e));
                        }
                    } else {
                        long nrOfUsers = 1L;
                        updatedMovieData.put("nrOfUsers", nrOfUsers);
                        groupMovies.document(String.valueOf(tmdbId))
                                .set(updatedMovieData)
                                .addOnFailureListener(e -> Log.e(TAG, "Updating movies (initiating nrOfUsers) in groups/{title}/movies failure: ", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Updating movies in groups/{title}/movies failure: ", e));
    }

    // Get users from root/groups/{groupTitle}/users
    // For every member -> delete users/{userId}/groups/{groupTitle}
    // Delete root/groups/{groupTitle}
    private void deleteGroup() {
        // Get all group members
        firestore.collection("groups").document(groupTitle).collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot userDocumentSnapshot : queryDocumentSnapshots) {
                        String userId = userDocumentSnapshot.getString("userId");
                        if (userId != null) {
                            firestore.collection("users").document(userId).collection("groups")
                                    .whereEqualTo("title", groupTitle)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                        for (QueryDocumentSnapshot groupDocumentSnapshot : queryDocumentSnapshots1) {
                                            // Delete group from member
                                            firestore.collection("users").document(userId).collection("groups").document(groupDocumentSnapshot.getId())
                                                    .delete();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Getting user from group (deleting) failure: ", e));

        // Delete group from groups
        DocumentReference groupReference = firestore.collection("groups").document(groupTitle);

        // Delete users collection in group
        groupReference.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String userGeneratedId = documentSnapshot.getId();
                        groupReference.collection("users").document(userGeneratedId)
                                .delete()
                                .addOnFailureListener(e -> Log.e(TAG, "Delete user in deleted group failure: ", e));
                    }
                });

        // Delete movies collection in group
        groupReference.collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String movieGeneratedId = documentSnapshot.getId();
                        groupReference.collection("movies").document(movieGeneratedId)
                                .delete()
                                .addOnFailureListener(e -> Log.e(TAG, "Delete movie in deleted group failure: ", e));
                    }
                });

        groupReference
                .delete()
                .addOnFailureListener(e -> Log.e(TAG, "Delete group failure: ", e));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setIsOwner() {
        firestore.collection("groups").document(groupTitle)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String ownerId = documentSnapshot.getString("ownerId");
                    isOwner = ownerId != null && ownerId.equals(firebaseAuth.getUid());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting ownerId: ", e));
    }
}
