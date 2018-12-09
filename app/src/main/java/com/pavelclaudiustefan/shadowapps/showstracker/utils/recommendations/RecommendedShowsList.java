package com.pavelclaudiustefan.shadowapps.showstracker.utils.recommendations;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.error.ANError;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.Show;
import com.pavelclaudiustefan.shadowapps.showstracker.utils.TmdbConstants;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.Headers;

// Class used to contain the recommended shows list and the methods used to create it
public abstract class RecommendedShowsList<T extends Show> {

    private static final String TAG = "ShadowDebug";

    private static final int SECOND_IN_MILLISECONDS = 1000;

    private String baseTmdbUrl;             // Base tmdbUrl used to get recommandations
    private ArrayList<Long> tmdbIds;        // List of ids of TMDb shows that are in the user's collection

    RecommendedShowsList(String baseTmdbUrl, long[] tmdbIds) {
        this.baseTmdbUrl = baseTmdbUrl;
        this.tmdbIds = new ArrayList<>();
        for (long tmdbId : tmdbIds) {
            this.tmdbIds.add(tmdbId);
        }
    }

    // For every tmdbId it requests maximum 20 recommended items and adds them to the adapter for displaying
    public void addRecommendedToList(ArrayList<T> shows) {
        Flowable.fromIterable(tmdbIds)
                .flatMap((Function<Long, Publisher<Pair<String, Long>>>) id -> Flowable.zip(getTitleFlowable(id),
                        Flowable.just(id),
                        Pair::new))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<Pair<String, Long>>() {
                    @Override
                    public void onNext(Pair<String, Long> pair) {
                        List<T> items = extractShowsFromJsonResponse(pair.first);
                        addOnlyUniqueItems(items, shows);
                        onDataIncremented();
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Recommended shows list onError() called: " + e.getMessage() + " " + e.toString(), e.getCause());
                    }
                    @Override
                    public void onComplete() {
                        sortItems(shows);
                        onDataLoaded();
                    }
                });
    }

    private Flowable<String> getTitleFlowable(Long id) {
        return Rx2AndroidNetworking.get(baseTmdbUrl + "{tmdbId}/recommendations")
                .addPathParameter("tmdbId", String.valueOf(id))
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
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

    // Removes items that are already in the user's collection, or that are already added to the totalItems list
    private void addOnlyUniqueItems(List<T> items, ArrayList<T> allItems) {
        for (T item : items) {
            long itemTmdbId = item.getTmdbId();
            boolean isUnique = true;

            // Check to see if is already in collection
            for (long tmdbId : tmdbIds) {
                if (itemTmdbId == tmdbId) {
                    isUnique = false;
                }
            }

            // If not in collection check to see if is already recommended
            if (isUnique) {
                for (T addedItem : allItems) {
                    if (itemTmdbId == addedItem.getTmdbId()) {
                        isUnique = false;
                        addedItem.incrementNrOfTimesRecommended();
                        break;
                    }
                }
            }

            // TODO - Optimize
            if (isUnique) {
                allItems.add(item);
            }
        }
    }

    public abstract void sortItems(ArrayList<T> items);

    public abstract List<T> extractShowsFromJsonResponse(String jsonResponse);

    public abstract void onDataIncremented();

    public abstract void onDataLoaded();
}
