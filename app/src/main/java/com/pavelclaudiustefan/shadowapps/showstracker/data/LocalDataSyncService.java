package com.pavelclaudiustefan.shadowapps.showstracker.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class LocalDataSyncService extends Service {

    public LocalDataSyncService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        // Notification progress bar
        // Sync movies
        // Sync tv shows


        Toast.makeText(this, "Service started - TODO sync data", Toast.LENGTH_SHORT).show();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
}
