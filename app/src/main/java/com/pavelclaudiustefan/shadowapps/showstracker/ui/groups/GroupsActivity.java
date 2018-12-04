package com.pavelclaudiustefan.shadowapps.showstracker.ui.groups;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.R;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.GroupListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

public class GroupsActivity extends BaseActivity {

    public static final String TAG = "GroupsActivity";

    @BindView(R.id.list)
    ListView groupListView;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;
    @BindView(R.id.empty_view)
    TextView emptyStateTextView;
    @BindView(R.id.fab_menu)
    FloatingActionsMenu floatingActionsMenu;
    @BindView(R.id.create_group)
    FloatingActionButton fabCreateGroup;
    @BindView(R.id.join_group)
    FloatingActionButton fabJoinGroup;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<String> groups;
    private GroupListAdapter groupListAdapter;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private Box<Movie> moviesBox;

    int lastGroupClicked;

    public GroupsActivity() {
        setLayout(R.layout.activity_groups);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Groups");
        navigationView.getMenu().findItem(R.id.nav_groups).setChecked(true);

        ButterKnife.bind(this);

        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        requestGroupsAndAddToAdapter();
        setUpListView();
        setUpFabMenu();
    }

    private void requestGroupsAndAddToAdapter() {
        groups = new ArrayList<>();
        requestGroups();
        groupListAdapter = new GroupListAdapter(this, groups);
        loadingIndicator.setVisibility(View.GONE);
    }

    // Realtime updates
    private void requestGroups() {
        loadingIndicator.setVisibility(View.VISIBLE);
        if (firebaseAuth.getUid() != null) {
            CollectionReference collectionReference = firestore.collection("users").document(firebaseAuth.getUid()).collection("groups");
            collectionReference
                    .orderBy("title", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(this, queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            groups.clear();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                String title = queryDocumentSnapshot.getString("title");
                                if (title == null) {
                                    Log.w(TAG, "group with null title field in firestore");
                                } else {
                                    groups.add(title);
                                }
                            }
                            groupListAdapter.notifyDataSetChanged();
                        } else {
                            Log.i(TAG, "requestGroups - Current data null");
                            setUpEmptyView(R.string.no_groups_added);
                        }
                        loadingIndicator.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        setUpEmptyView(R.string.connection_issues);
                        loadingIndicator.setVisibility(View.GONE);
                        Log.e(TAG, "getting groups failure", e);
                    });
        } else {
            Log.e(TAG, "FirebaseAuth.getUid() == null");
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private void setUpEmptyView(int resid) {
        //Only visible if no groups are found
        emptyStateTextView.setVisibility(View.VISIBLE);
        emptyStateTextView.setText(resid);
        groupListView.setEmptyView(emptyStateTextView);
    }

    private void setUpListView() {
        groupListView.setAdapter(groupListAdapter);

        // Setup the item click listener
        groupListView.setOnItemClickListener((adapterView, view, position, id) -> {
            Intent intent = new Intent(GroupsActivity.this, GroupActivity.class);
            String group = groups.get(position);
            intent.putExtra("group_title", group);
            lastGroupClicked = position;
            startActivityForResult(intent, 1);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Returning from activity where group was deleted
            // Force update
            groups.remove(lastGroupClicked);
            groupListAdapter.notifyDataSetChanged();
            if (groups.isEmpty()) {
                setUpEmptyView(R.string.no_groups_added);
            }
        }
    }

    private void setUpFabMenu() {
        fabCreateGroup.setOnClickListener(view -> {
            showCreateGroupDialog();
            floatingActionsMenu.collapse();
        });
        fabJoinGroup.setOnClickListener(view -> {
            showJoinGroupDialog();
            floatingActionsMenu.collapse();
        });
    }

    private void showCreateGroupDialog() {
        AlertDialog.Builder createGroupDialogBuilder = new AlertDialog.Builder(this);
        createGroupDialogBuilder
                .setTitle("Create group")
                .setView(R.layout.dialog_group_create_join)
                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                    EditText groupTitleEditText = ((AlertDialog)dialogInterface).findViewById(R.id.group_title);
                    String groupTitle = groupTitleEditText.getText().toString();
                    tryCreateGroup(groupTitle);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        createGroupDialogBuilder.create().show();
    }

    // Check if group already exists in root/groups
    //   If not -> add group to - root/groups
    //                          - root/users/{userId}/groups
    private void tryCreateGroup(String title) {
        if (title != null && !title.isEmpty() && firebaseAuth.getUid() != null) {
            firestore.collection("groups").document(title)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // Group already exists -> Show info toast
                                Toast.makeText(GroupsActivity.this, "Group already exists", Toast.LENGTH_LONG).show();
                            } else {
                                // Group doesn't exist -> Create it
                                createGroup(title);
                            }
                        } else {
                            Log.d(TAG, "tryCreateGroup() - failed with ", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "GroupsActivity tryCreateGroup() - Error");
        }
    }

    // Add group to - root/groups
    //              - root/users/{ownerId}/groups
    // Add unwatch movies to root/groups/{groupTitle}/movies for init
    private void createGroup(String title) {
        if (firebaseAuth.getCurrentUser()!= null && firebaseAuth.getUid() != null) {
            // Add group to root/groups
            Map<String, Object> completeData = new HashMap<>();
            completeData.put("title", title);
            completeData.put("ownerId", firebaseAuth.getUid());
            completeData.put("createdAt", Timestamp.now());
            completeData.put("updatedAt", Timestamp.now());
            firestore.collection("groups").document(title)
                    .set(completeData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Group successfully created");

                        // Add group to root/users/{ownerId}/groups
                        Map<String, Object> essentialData = new HashMap<>();
                        essentialData.put("title", title);
                        firestore.collection("users").document(firebaseAuth.getUid()).collection("groups")
                                .document(title)
                                .set(essentialData)
                                .addOnSuccessListener(documentReference -> Log.d(TAG, "User group successfully added"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error adding user group", e));

                        // Add owner to groups/{groupTitle}/users for init
                        String displayName = firebaseAuth.getCurrentUser().getEmail();
                        if (firebaseAuth.getCurrentUser().getDisplayName() != null && !firebaseAuth.getCurrentUser().getDisplayName().isEmpty()) {
                            displayName = firebaseAuth.getCurrentUser().getDisplayName();
                        }
                        HashMap<String, Object> user = new HashMap<>();
                        user.put("displayName", displayName);
                        user.put("userId", firebaseAuth.getUid());
                        firestore.collection("groups").document(title).collection("users")
                                .document(firebaseAuth.getUid())
                                .set(user)
                                .addOnSuccessListener(documentReference -> Log.d(TAG, "(Init) User successfully added!"))
                                .addOnFailureListener(e -> Log.w(TAG, "(Init) Error adding user", e));

                        // Add every unwatched movie (belonging to owner) to the group for init
                        List<Movie> movies = moviesBox.getAll();
                        for (Movie movie : movies) {
                            long nrOfUsers = 1; // used to initiate
                            if (movie.isWatched()) {
                                nrOfUsers = 0;
                            }
                            Map<String, Object> movieData = new HashMap<>();
                            movieData.put("tmdbId", movie.getTmdbId());
                            movieData.put("nrOfUsers", nrOfUsers);
                            firestore.collection("groups").document(title).collection("movies")
                                    .document(String.valueOf(movie.getTmdbId()))
                                    .set(movieData)
                                    .addOnSuccessListener(documentReference -> Log.d(TAG, "(Init) Movie successfully added!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "(Init) Error adding movie", e));
                        }
                        requestGroups();
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error creating group", e));
        }
    }

    // Shows a dialog with an EditText that allows the user to input the name of the group he wants to join
    private void showJoinGroupDialog() {
        AlertDialog.Builder createGroupDialogBuilder = new AlertDialog.Builder(this);
        createGroupDialogBuilder
                .setTitle("Join group")
                .setView(R.layout.dialog_group_create_join)
                .setPositiveButton("Confirm", (dialogInterface, i) -> {
                    EditText groupTitleEditText = ((AlertDialog)dialogInterface).findViewById(R.id.group_title);
                    String groupTitle = groupTitleEditText.getText().toString();
                    tryJoinGroup(groupTitle);
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        createGroupDialogBuilder.create().show();
    }

    // Check if group exists in root/groups
    //   If yes -> Check if not already joined
    //               If no -> add current user to root/groups{groupTitle}/users
    //                     -> add group to root/users/{userId}/groups
    private void tryJoinGroup(String title) {
        if (title != null && !title.isEmpty() && firebaseAuth.getUid() != null) {
            loadingIndicator.setVisibility(View.VISIBLE);
            firestore.collection("groups").document(title)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                // Group already exists -> Check if not already joined
                                firestore.collection("groups").document(title).collection("users")
                                        .whereEqualTo("userId", firebaseAuth.getUid())
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                // User is not a member of the group already -> Join group
                                                joinGroup(title);
                                            } else {
                                                // Already joined -> Show info toast
                                                loadingIndicator.setVisibility(View.GONE);
                                                Toast.makeText(GroupsActivity.this, "Already joined", Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "GetUsers from group failure: ", e));
                            } else {
                                // Group doesn't exist -> Show toast
                                loadingIndicator.setVisibility(View.GONE);
                                Toast.makeText(GroupsActivity.this, "Group doesn't exist", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            loadingIndicator.setVisibility(View.GONE);
                            Log.d(TAG, "tryJoinGroup() - failed with ", task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "GroupsActivity tryJoinGroup() - Error");
        }
    }

    // Updated group ("updatedAt" field)
    // Add user to root/groups{groupTitle}/users
    // Add group to root/users/{userId}/groups
    private void joinGroup(String title) {
        if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getUid() != null) {
            // Updated updatedAt field in joined group
            firestore.collection("groups").document(title)
                    .update("updatedAt", Timestamp.now())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Group successfully updated"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating group", e));

            // Add user to groups/{groupTitle}/users
            String displayName = firebaseAuth.getCurrentUser().getEmail();
            if (firebaseAuth.getCurrentUser().getDisplayName() != null && !firebaseAuth.getCurrentUser().getDisplayName().isEmpty()) {
                displayName = firebaseAuth.getCurrentUser().getDisplayName();
            }
            HashMap<String, Object> user = new HashMap<>();
            user.put("displayName", displayName);
            user.put("userId", firebaseAuth.getUid());
            firestore.collection("groups").document(title).collection("users")
                    .document(firebaseAuth.getUid())
                    .set(user)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "(Init) User successfully added!"))
                    .addOnFailureListener(e -> Log.w(TAG, "(Init) Error adding user", e));

            // Add group to root/users/{ownerId}/groups
            Map<String, Object> essentialData = new HashMap<>();
            essentialData.put("title", title);
            firestore.collection("users").document(firebaseAuth.getUid()).collection("groups")
                    .document(title)
                    .set(essentialData)
                    .addOnSuccessListener(documentReference ->  {
                        requestGroups();
                        Log.d(TAG, "User group successfully added");
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding user group", e));

            updateGroupMovies(title);
        }
    }

    // For every movie from user's list
    //   If movie is not in groups/{grouptTitle}/movies
    //     Add -> "tmdbId" movie.getTmdbId() and initiate "nrOfUsers" with 1
    //   Else
    //     Add "tmdbId" movie.getTmdbId() and increment "nrOfUsers"
    private void updateGroupMovies(String title) {
        // Add every valid (that every member wants to watch) unwatched movie to the group
        List<Movie> movies = moviesBox.getAll();
        CollectionReference groupMovies = firestore.collection("groups").document(title).collection("movies");
        for (Movie movie : movies) {
            groupMovies.document(String.valueOf(movie.getTmdbId()))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Map<String, Object> updatedMovieData = new HashMap<>();
                        updatedMovieData.put("tmdbId", movie.getTmdbId());
                        if (documentSnapshot.exists()) {
                            Long nrOfUsers = documentSnapshot.getLong("nrOfUsers");
                            if (nrOfUsers != null) {
                                if (movie.isWatched()) {
                                    updatedMovieData.put("nrOfUsers", nrOfUsers);
                                } else {
                                    updatedMovieData.put("nrOfUsers", nrOfUsers+1);
                                }
                                groupMovies.document(String.valueOf(movie.getTmdbId()))
                                        .set(updatedMovieData)
                                        .addOnFailureListener(e -> Log.e(TAG, "Updating movies (incrementing nrOfUsers) in groups/{title}/movies failure: ", e));
                            }
                        } else {
                            int nrOfUsers = 0;
                            if (!movie.isWatched()) {
                                nrOfUsers = 1;
                            }
                            updatedMovieData.put("nrOfUsers", nrOfUsers);
                            groupMovies.document(String.valueOf(movie.getTmdbId()))
                                    .set(updatedMovieData)
                                    .addOnFailureListener(e -> Log.e(TAG, "Updating movies (initiating nrOfUsers) in groups/{title}/movies failure: ", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Updating movies in groups/{title}/movies failure: ", e));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_groups) {
            closeDrawer();
            return true;
        } else {
            return super.onNavigationItemSelected(item);
        }
    }

}
