package com.pavelclaudiustefan.shadowapps.showstracker;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.pavelclaudiustefan.shadowapps.showstracker.data.models.MyObjectBox;

import io.objectbox.BoxStore;

public class MyApp extends Application {

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());

        boxStore = MyObjectBox.builder().androidContext(MyApp.this).build();
//        http://localhost:8090/index.html
//        adb forward tcp:8090 tcp:8090

//        if (BuildConfig.DEBUG) {
//            boolean started = new AndroidObjectBrowser(boxStore).start(this);
//            Log.i("ObjectBrowser", "Started: " + started);
//        }
    }

    public BoxStore getBoxStore() {
        if (boxStore.isClosed()) {
            boxStore = MyObjectBox.builder().androidContext(MyApp.this).build();
        }

        return boxStore;
    }

}
