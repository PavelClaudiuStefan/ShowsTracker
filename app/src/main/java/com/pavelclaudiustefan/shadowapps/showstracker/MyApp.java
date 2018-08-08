package com.pavelclaudiustefan.shadowapps.showstracker;

import android.app.Application;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.pavelclaudiustefan.shadowapps.showstracker.models.MyObjectBox;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class MyApp extends Application {

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());

        // http://localhost:8090/index.html
        // adb forward tcp:8090 tcp:8090
        boxStore = MyObjectBox.builder().androidContext(MyApp.this).build();
        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(this);
            Log.i("ObjectBrowser", "Started: " + started);
        }

        Log.d("MyApp", "Using ObjectBox " + BoxStore.getVersion() + " (" + BoxStore.getVersionNative() + ")");
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

}
