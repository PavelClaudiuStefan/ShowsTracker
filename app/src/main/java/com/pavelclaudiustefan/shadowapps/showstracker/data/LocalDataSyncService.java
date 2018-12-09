package com.pavelclaudiustefan.shadowapps.showstracker.data;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Pair;

import com.androidnetworking.error.ANError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pavelclaudiustefan.shadowapps.showstracker.MyApp;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.QueryUtils;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.Headers;

public class LocalDataSyncService extends Service {

    private static final int PROGRESS_NOTIFICATION_ID = 19960409;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notificationBuilder;

    private ArrayList<Movie> movies;
    private int nrOfMovies;
    private double currentPercentage = 0;

    private static final int SECOND_IN_MILLISECONDS = 1000;

    public LocalDataSyncService() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSync();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    private void startSync() {
        initNotification();

        getMovieIdsFromFirestore();
    }

    /**
     * This method starts the process of syncing local data with server data (firestore)
     * After successfully getting the movie ids, they are sent to the getMoviesFromTmdb() method
     */
    private void getMovieIdsFromFirestore() {
        updateNotification(5);

        String userID = firebaseAuth.getUid();

        if (userID != null) {
            DocumentReference groupReference = firestore.collection("users").document(userID);
            groupReference.collection("movies")
                    .get()
                    .addOnSuccessListener(moviesSnapshots -> {
                        if (moviesSnapshots != null && !moviesSnapshots.isEmpty()) {
                            ArrayList<String> movieIds = new ArrayList<>();
                            ArrayList<Boolean> isWatchedMovies = new ArrayList<>();
                            for (QueryDocumentSnapshot movieSnapshot : moviesSnapshots) {
                                movieIds.add(movieSnapshot.getId());
                                isWatchedMovies.add(movieSnapshot.getBoolean("isWatched"));
                            }
                            getMoviesFromTmdb(movieIds, isWatchedMovies);
                            updateNotification(10);
                            nrOfMovies = movieIds.size();
                        } else {
                            Log.w("ShadowDebug", "movieSnapshots null or empty");
                            finishNotification(true);
                        }
                    })
                    .addOnCanceledListener(() -> {
                        Log.w("ShadowDebug", "Firestore movie ids querry canceled");
                        finishNotification(false);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ShadowDebug", "Firestore movie ids querry failed", e);
                        finishNotification(false);
                    });
        } else {
            Log.e("ShadowDebug", "ERROR: userID is null");
        }
    }

    /**
     * This method gets movies data from TMDb using each id in the movieIds list
     * After each respective movie gets received from the API, the notification progress is updated
     * When every movie is received, the list of movies is sent to the storeMovies() method
     * @param movieIds is a list of TMDb ids of movies
     * @param isWatchedMovies is a list of boolean values, isWatchedMovies.get(i) is true if the movie with the id = movieIds.get(i) is watched
     */
    private void getMoviesFromTmdb(List<String> movieIds, List<Boolean> isWatchedMovies) {
        movies = new ArrayList<>();

        Flowable.fromIterable(movieIds)
                .flatMap((Function<String, Publisher<Pair<String, String>>>) id -> Flowable.zip(getMovieFlowable(id),
                        Flowable.just(id),
                        Pair::new))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<Pair<String, String>>() {
                    @Override
                    public void onNext(Pair<String, String> pair) {
                        Movie movie = QueryUtils.extractMovieDataFromJson(pair.first);
                        int movieIndex = movieIds.indexOf(pair.second);

                        if (movie != null) {
                            movie.setWatched(isWatchedMovies.get(movieIndex));
                        } else {
                            finishNotification(false);
                        }

                        movies.add(movie);
                        onDataIncremented();
                    }
                    @Override
                    public void onError(Throwable e) {
                        //Log.e("ShadowDebug", "LocalDataSyncService - onError() called: " + ((ANError)e).getErrorBody(), e);
                        Log.e("ShadowDebug", "LocalDataSyncService - onError() called: " + e.toString(), e);
                        finishNotification(false);
                    }
                    @Override
                    public void onComplete() {
                        onDataLoaded();
                    }
                });
    }

    private Flowable<String> getMovieFlowable(String id) {
        return Rx2AndroidNetworking.get(TmdbConstants.MOVIES_URL + "{tmdbId}")
                .addPathParameter("tmdbId", id)
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
                .addQueryParameter("append_to_response", "release_dates")
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .getStringFlowable()
                .retryWhen(new RetryWithDelay(3));
    }

    private class RetryWithDelay implements Function<Flowable<? extends Throwable>, Flowable<?>> {
        private final int maxRetries;
        private int retryCount;

        RetryWithDelay(final int maxRetries) {
            this.maxRetries = maxRetries;
            this.retryCount = 0;
        }

        @Override
        public Flowable<?> apply(final Flowable<? extends Throwable> attempts) {
            return attempts
                    .flatMap((Function<Throwable, Flowable<?>>) throwable -> {
                        Headers headers = ((ANError)throwable).getResponse().headers();
                        int retryAfter = 1 + Integer.valueOf(headers.get("Retry-After"));

                        if (++retryCount < maxRetries) {
                            return Flowable.timer(retryAfter * SECOND_IN_MILLISECONDS,
                                    TimeUnit.MILLISECONDS);
                        }

                        return Flowable.error(throwable);
                    });
        }
    }

    private void onDataIncremented() {
        double percentage = currentPercentage + ((double)90/nrOfMovies);
        updateNotification(percentage);
    }

    private void onDataLoaded() {
        storeMovies(movies);
    }

    /**
     * This method stores the movies list in local storage (ObjectBox)
     * Aftering storing the movies, it updates the notification
     * @param movies is a list of movies of type {@link com.pavelclaudiustefan.shadowapps.showstracker.data.models.Movie}
     */
    private void storeMovies(List<Movie> movies) {
        Box<Movie> movieBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);
        movieBox.put(movies);
        finishNotification(true);
    }

    private void initNotification() {
        notificationManagerCompat = NotificationManagerCompat.from(LocalDataSyncService.this);

        notificationBuilder = createNotificationBuilder();
        notificationBuilder.setTicker("Start syncing local data from the server");
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setContentTitle("Syncing local data");
        notificationBuilder.setContentText("0%");
        notificationBuilder.setProgress(100, 0, false);
        notificationManagerCompat.notify(PROGRESS_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotification(double percentage) {
        if (percentage > 100)
            percentage = 100;

        if (percentage > currentPercentage) {
            int intPercentage = (int)(percentage + 0.5);

            notificationBuilder.setContentText(intPercentage + "%");
            notificationBuilder.setProgress(100, intPercentage, false);
            notificationManagerCompat.notify(PROGRESS_NOTIFICATION_ID, notificationBuilder.build());
            currentPercentage = percentage;
        }
    }

    private void finishNotification(boolean isSuccessful) {
        new Handler().postDelayed(() -> {
            String statusText = isSuccessful ? "Done" : "Fail";
            int resId = isSuccessful ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_notify_error;

            notificationBuilder.setContentTitle("Syncing data");
            notificationBuilder.setSmallIcon(resId);
            notificationBuilder.setOngoing(false);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentText(statusText);
            notificationBuilder.setProgress(0, 0, false);
            notificationManagerCompat.notify(PROGRESS_NOTIFICATION_ID, notificationBuilder.build());
        }, 250);
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        String channelId = "sync_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "Local data sync progress bar";
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        return new NotificationCompat.Builder(this, channelId);
    }
}
