package com.pavelclaudiustefan.shadowapps.showstracker;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.facebook.FacebookSdk;
import com.pavelclaudiustefan.shadowapps.showstracker.models.MyObjectBox;

import io.objectbox.BoxStore;

public class MyApp extends Application {

    private BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext(), 100);

        boxStore = MyObjectBox.builder().androidContext(MyApp.this).build();
//        http://localhost:8090/index.html
//        adb forward tcp:8090 tcp:8090

//        if (BuildConfig.DEBUG) {
//            boolean started = new AndroidObjectBrowser(boxStore).start(this);
//            Log.i("ObjectBrowser", "Started: " + started);
//        }
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }

}
