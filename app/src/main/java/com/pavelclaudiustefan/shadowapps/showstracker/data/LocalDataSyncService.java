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

    public static final String OPTION_TITLE = "titleOption";
    public static final int TITLE_SYNC = 0;
    public static final int TITLE_UPDATE = 1;

    private static final int PROGRESS_NOTIFICATION_ID = 19960409;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    Box<Movie> moviesBox;

    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notificationBuilder;

    private int titleVariant;
    private String[] titles = new String[2];

    private int nrOfMovies;
    private double currentPercentage = 0;

    private static final int SECOND_IN_MILLISECONDS = 1000;

    public LocalDataSyncService() {
        titles[TITLE_SYNC] = "Syncing data";
        titles[TITLE_UPDATE] = "Updating data";

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        titleVariant = intent.getIntExtra(OPTION_TITLE, TITLE_SYNC);
        moviesBox = ((MyApp) getApplication()).getBoxStore().boxFor(Movie.class);

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
     * When every movie is received, the list of movies is sent to the storeMovie() method
     * @param movieIds is a list of TMDb ids of movies
     * @param isWatchedMovies is a list of boolean values, isWatchedMovies.get(i) is true if the movie with the id = movieIds.get(i) is watched
     */
    private void getMoviesFromTmdb(List<String> movieIds, List<Boolean> isWatchedMovies) {
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
                        onDataIncremented(movie);
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

    private void onDataIncremented(Movie movie) {
        storeMovie(movie);
        double percentage = currentPercentage + ((double)90/nrOfMovies);
        updateNotification(percentage);
    }

    private void onDataLoaded() {
        finishNotification(true);
    }

    private void storeMovie(Movie movie) {
        moviesBox.put(movie);
    }

    private void initNotification() {
        String contentTitle = titles[titleVariant];
        String ticker;
        if (titleVariant == TITLE_SYNC) {
            ticker = "Start syncing local data from the server";
        } else {
            ticker = "Start updating data";
        }

        notificationManagerCompat = NotificationManagerCompat.from(LocalDataSyncService.this);
        notificationBuilder = createNotificationBuilder();
        notificationBuilder.setTicker(ticker);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        notificationBuilder.setContentTitle(contentTitle);
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
        String statusText = isSuccessful ? "Done" : "Fail";
        int resId = isSuccessful ? android.R.drawable.stat_sys_download_done : android.R.drawable.stat_notify_error;
        String contentTitle = titles[titleVariant];

        // In case of not proper;y
        new Handler().postDelayed(() -> {
            notificationBuilder.setContentTitle(contentTitle);
            notificationBuilder.setSmallIcon(resId);
            notificationBuilder.setOngoing(false);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentText(statusText);
            notificationBuilder.setProgress(0, 0, false);
            notificationManagerCompat.notify(PROGRESS_NOTIFICATION_ID, notificationBuilder.build());
        }, 1000);
    }

    private NotificationCompat.Builder createNotificationBuilder() {
        String channelId;
        String channelName;
        if (titleVariant == TITLE_SYNC) {
            channelId = "sync_channel";
            channelName = "Sync progress bar";
        }
        else {
            channelId = "update_channel";
            channelName = "Update progress bar";
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
