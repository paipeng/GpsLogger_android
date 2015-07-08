package com.paipeng.gpslogger;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.content.ClipboardManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paipeng.gpslogger.services.GPSService;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity {
    private static final String TAG  = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Intent gpsServiceIntent;
    private GPSBroadcastReceiver gpsBroadcastReceiver;

    public static final String GPS_START = "GPS_START";
    public static final String GPS_STOP = "GPS_STOP";
    public static final String GPS_LOCATION = "GPS_LOCATION";


    public ArrayList<LatLng> latLngArrayList;
    private Marker spotMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(TAG, "onMapClick");

                if (spotMarker != null) {
                    spotMarker.remove();
                }
                MarkerOptions spotMarkerOptions = new MarkerOptions();
                spotMarkerOptions.position(latLng).title(latLng.toString());
                spotMarker = mMap.addMarker(spotMarkerOptions);
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String text = String.valueOf(marker.getPosition().latitude) + "," + String.valueOf(marker.getPosition().longitude);
                Log.i(TAG, "text " + text);

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("gps", text);
                clipboard.setPrimaryClip(clip);

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });


        setUpMapIfNeeded();

        latLngArrayList = new ArrayList<>();
        gpsServiceIntent =
                new Intent(this, GPSService.class);

        gpsBroadcastReceiver = new GPSBroadcastReceiver();


        startService(gpsServiceIntent);

        /*
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        // Registers the DownloadStateReceiver and its intent filters
        IntentFilter gpsIntentFilter = new IntentFilter("GPS");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                gpsBroadcastReceiver, gpsIntentFilter);
    }

    @Override
    public void onDestroy() {
        // Unregisters the FragmentDisplayer instance
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gpsBroadcastReceiver);
        // Must always call the super method at the end.
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialog_title);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialog_text)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_button_yes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                        stopService(gpsServiceIntent);
                        MapsActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                        MapsActivity.this.finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    private void makeUseOfNewLocation(Location location) {
        Log.i(TAG, "makeUseOfNewLocation");

        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            latLngArrayList.add(currentLatLng);
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(currentLatLng)) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 7.0f));
            }
            mMap.clear();


            for (LatLng latLng : latLngArrayList) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("was").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }

            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Position"));
        }

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap(LatLng latLng)} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(new LatLng(0, 0));
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
    }

    private class GPSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "GPSBroadcastReceiver onReceive");
            Location currentLocation = intent.getParcelableExtra(GPS_LOCATION);
            if (currentLocation != null) {
                makeUseOfNewLocation(currentLocation);
            }
        }
    }

}
