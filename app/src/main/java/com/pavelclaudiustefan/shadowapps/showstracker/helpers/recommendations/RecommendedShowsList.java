package com.pavelclaudiustefan.shadowapps.showstracker.helpers.recommendations;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.AnalyticsListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pavelclaudiustefan.shadowapps.showstracker.adapters.ShowItemListAdapter;
import com.pavelclaudiustefan.shadowapps.showstracker.helpers.TmdbConstants;
import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

// Class used to contain the recommended shows list and the methods used to create it
public abstract class RecommendedShowsList<T extends Show> {

    private String TAG = "RecommendedShowsList";

    private String baseTmdbUrl;         // Base tmdbUrl used to get recommandations
    private long[] tmdbIds;    // List of ids of TMDb shows that are in the user's collection

    RecommendedShowsList(String baseTmdbUrl, long[] tmdbIds) {
        this.baseTmdbUrl = baseTmdbUrl;
        this.tmdbIds = tmdbIds;
    }

    public ArrayList<T> getList() {
        final ArrayList<T> totalShows = new ArrayList<>();

        for (long tmdbId : tmdbIds) {
            AndroidNetworking.get(baseTmdbUrl + "{tmdbId}/recommendations")
                    .addPathParameter("api_key", TmdbConstants.API_KEY)
                    .addQueryParameter("tmdbId", String.valueOf(tmdbId))
                    .setTag(this)
                    .setPriority(Priority.LOW)
                    .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                    .build()
                    .setAnalyticsListener(new AnalyticsListener() {
                        @Override
                        public void onReceived(long timeTakenInMillis, long bytesSent, long bytesReceived, boolean isFromCache) {
                            Log.d(TAG, "\ntimeTakenInMillis : " + timeTakenInMillis + " isFromCache : " + isFromCache);
                        }
                    })
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            List<T> items = extractShowsFromJsonResponse(response);
                            addOnlyUniqueItems(items, totalShows);
                            totalShows.addAll(items);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e("ShadowDebug", anError.getErrorBody());
                        }
                    });
        }
        return sortItems(totalShows);
    }

    private Flowable<String> getTitleFlowable(Long id) {

        return Rx2AndroidNetworking.get(baseTmdbUrl + "{tmdbId}/recommendations")
                .addPathParameter("tmdbId", String.valueOf(id))
                .addQueryParameter("api_key", TmdbConstants.API_KEY)
                .setMaxAgeCacheControl(10, TimeUnit.MINUTES)
                .build()
                .getStringFlowable();
    }

    // For every tmdbId it requests maximum 20 recommended items and adds them to the adapter for displaying
    public void addRecommendedToAdapter(final ShowItemListAdapter<T> showItemListAdapter) {
        final ArrayList<Long> tmdbIdsLong = new ArrayList<>();
        for (long tmdbId : tmdbIds) {
            tmdbIdsLong.add(tmdbId);
        }

        final ArrayList<T> allItems = new ArrayList<>();

        Flowable.fromIterable(tmdbIdsLong)
                .flatMap(new Function<Long, Publisher<Pair<String, Long>>>() {
                    @Override
                    public Publisher<Pair<String, Long>> apply(Long id) {
                        return Flowable.zip(getTitleFlowable(id),
                                Flowable.just(id),
                                new BiFunction<String, Long, Pair<String, Long>>() {
                                    @Override
                                    public Pair<String, Long> apply(String jsonResponse, Long id) {
                                        return new Pair<>(jsonResponse, id);
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<Pair<String, Long>>() {
                    @Override
                    public void onNext(Pair<String, Long> pair) {
                        List<T>items = extractShowsFromJsonResponse(pair.first);
                        addOnlyUniqueItems(items, allItems);
                        //showItemListAdapter.addAll(items);
                        onDataIncremented();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ShadowDebug", "Recommended shows list onError");//e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        showItemListAdapter.addAll(sortItems(allItems));
                        onDataLoaded();
                    }
                });
    }

    // Removes items that are already in the user's collection, or that are already added to the totalItems list
    private void addOnlyUniqueItems(List<T> items, ArrayList<T> allItems) {
        for (T item : items) {
            long itemTmdbId = item.getTmdbId();
            boolean isUnique = true;

            for (long tmdbId : tmdbIds) {
                if (itemTmdbId == tmdbId) {
                    isUnique = false;
                }
            }

            for (T addedItem : allItems) {
                if (itemTmdbId == addedItem.getTmdbId()) {
                    isUnique = false;
                }
            }

            if (isUnique) {
                allItems.add(item);
            }
        }
    }

    public abstract ArrayList<T> sortItems(ArrayList<T> items);

    public abstract List<T> extractShowsFromJsonResponse(String jsonResponse);

    public abstract void onDataIncremented();

    public abstract void onDataLoaded();
}
