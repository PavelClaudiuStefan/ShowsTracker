package com.pavelclaudiustefan.shadowapps.showstracker.utils.recommendations;

import android.util.Log;
import android.util.Pair;

import com.pavelclaudiustefan.shadowapps.showstracker.models.Show;
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

// Class used to contain the recommended shows list and the methods used to create it
public abstract class RecommendedShowsList<T extends Show> {

    private String baseTmdbUrl;         // Base tmdbUrl used to get recommandations
    private long[] tmdbIds;             // List of ids of TMDb shows that are in the user's collection

    RecommendedShowsList(String baseTmdbUrl, long[] tmdbIds) {
        this.baseTmdbUrl = baseTmdbUrl;
        this.tmdbIds = tmdbIds;
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
    public void addRecommendedToList(ArrayList<T> shows) {
        final ArrayList<Long> tmdbIdsLong = new ArrayList<>();
        for (long tmdbId : tmdbIds) {
            tmdbIdsLong.add(tmdbId);
        }

        final ArrayList<T> allItems = new ArrayList<>();

        Flowable.fromIterable(tmdbIdsLong)
                .flatMap((Function<Long, Publisher<Pair<String, Long>>>) id -> Flowable.zip(getTitleFlowable(id),
                        Flowable.just(id),
                        Pair::new))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<Pair<String, Long>>() {
                    @Override
                    public void onNext(Pair<String, Long> pair) {
                        List<T> items = extractShowsFromJsonResponse(pair.first);
                        addOnlyUniqueItems(items, allItems);
                        onDataIncremented();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("ShadowDebug", "Recommended shows list onError");//e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        shows.addAll(sortItems(allItems));
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

            // TODO - Optimize
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
