package com.paipeng.gpslogger.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.paipeng.gpslogger.MapsActivity;

/**
 * Created by paipeng on 08.07.15.
 */
public class GPSService extends Service {
    private static final String TAG = GPSService.class.getSimpleName();
    // Defines and instantiates an object for handling status updates.

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startGPS();
        //return Service.START_NOT_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onHandleIntent action " + action);
        if (MapsActivity.GPS_START.equals(action)) {
            startGPS();
        }
    }

    private void startGPS() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
                notifyProgress(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 5, locationListener);
    }

    public void notifyProgress(Location location) {
        Log.d(TAG, "notifyProgress " + location.getTime());
        Intent localIntent = new Intent("GPS");

        // Puts log data into the Intent
        localIntent.putExtra(MapsActivity.GPS_LOCATION, location);

        // Broadcasts the Intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }
}
