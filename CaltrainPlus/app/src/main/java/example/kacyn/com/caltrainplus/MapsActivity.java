package example.kacyn.com.caltrainplus;

import android.Manifest;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;


import java.util.ArrayList;

import example.kacyn.com.caltrainplus.data.StationContract.StationEntry;
import example.kacyn.com.caltrainplus.data.StationDbHelper;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>,
        LocationListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final float GEOFENCE_RADIUS_METERS = 1609; // 1 mile
    private static final int GEOFENCE_EXPIRATION_MS = 5 * 60 * 60 * 1000; //to expire in 5 hours
    private static final int DEFAULT_ZOOM_LEVEL = 10;
    public static final long LOCATION_UPDATE_INTERVAL_MS = 10000;
    public static final long FASTEST_LOCATION_UPDATE_INTERVAL_MS = LOCATION_UPDATE_INTERVAL_MS / 2;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private boolean mPermissionDenied = false;

    protected GoogleApiClient mGoogleApiClient;

    StationDbHelper mDbHelper;
    SharedPreferences mPrefs;
    String mDestination;
    PendingIntent mGeofencePendingIntent;
    Geofence mGeofence;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    double mDestinationLat;
    double mDestinationLng;
    float mDistancetoDestMiles;
    ShareActionProvider mShareActionProvider;

    private static final int STATION_LOADER = 0;

    private static String[] STATION_COLUMNS = {
            StationEntry._ID,
            StationEntry.COLUMN_STATION_NAME,
            StationEntry.COLUMN_STATION_LAT,
            StationEntry.COLUMN_STATION_LNG
    };

    static final int COL_STATION_ID = 0;
    static final int COL_STATION_NAME = 1;
    static final int COL_STATION_LAT = 2;
    static final int COL_STATION_LNG = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDestination = mPrefs.getString(getString(R.string.station_key), "");
        mDbHelper = new StationDbHelper(this);

        //initialize pending intent
        mGeofencePendingIntent = null;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();

        getLoaderManager().initLoader(STATION_LOADER, null, this);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(STATION_LOADER, null, this);
        setUpMapIfNeeded();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            //stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        Log.v(TAG, "starting location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setFastestInterval(FASTEST_LOCATION_UPDATE_INTERVAL_MS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setOnMyLocationButtonClickListener(this);
                enableMyLocation();

            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "My location clicked", Toast.LENGTH_SHORT).show();

        return false;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                StationEntry.CONTENT_URI,
                STATION_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        while (c.moveToNext()) {
            double lat = c.getDouble(COL_STATION_LAT);
            double lng = c.getDouble(COL_STATION_LNG);
            String stationName = c.getString(COL_STATION_NAME);

//            Log.v(TAG, "lat: " + lat + " long: " + lng + " stationName: " + stationName);
//
//            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(stationName));

            if (stationName.equals(mDestination)) {
                addDestinationMarker(lat, lng, stationName);

                Log.v(TAG, "destination: " + mDestination);
                addGeofence(lat, lng);
                mDestinationLat = lat;
                mDestinationLng = lng;
//                addGeofence(37.331687, -122.02646600);
            } else {
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(stationName));
            }
        }
    }

    void addDestinationMarker(double lat, double lng, String stationName) {
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setStyle(IconGenerator.STYLE_GREEN);

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(stationName)
                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(stationName))));
    }

    private void addGeofence(double lat, double lng) {

        Log.v(TAG, "geofence created at lat: " + lat + " long: " + lng);

        mGeofence = new Geofence.Builder()
                .setRequestId(getString(R.string.geofence_request_id))
                .setCircularRegion(lat, lng, GEOFENCE_RADIUS_METERS)
                .setExpirationDuration(GEOFENCE_EXPIRATION_MS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder geoRequestBuilder = new GeofencingRequest.Builder();
        geoRequestBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        geoRequestBuilder.addGeofence(mGeofence);
        return geoRequestBuilder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Log.v(TAG, "Creating new geofence service");
        Intent intent = new Intent(this, GeofenceService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            Log.v(TAG, "current location.  lat: " + mLastLocation.getLatitude() + " long: " + mLastLocation.getLongitude());
            //zoom to current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), DEFAULT_ZOOM_LEVEL));
        }

        startLocationUpdates();

        PendingIntent pendingIntent = getGeofencePendingIntent();

        if (pendingIntent == null) {
            Log.v(TAG, "pending intent is null ");
        }

        if (mGeofence != null) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent())
                    .setResultCallback(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) Log.v(TAG, "geofence successfully created");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        float[] results = new float[1];
        Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mDestinationLat, mDestinationLng, results);

        mDistancetoDestMiles = results[0] / 1609;

        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(getString(R.string.distance_key), mDistancetoDestMiles);
        editor.apply();

        //update share intent
        mShareActionProvider.setShareIntent(createShareIntent());

        //update camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), DEFAULT_ZOOM_LEVEL));

        //update widget
        Intent widgetIntent = new Intent(this, InfoWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, InfoWidget.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetIntent);

        Log.v(TAG, "location changed. lat: " + mLastLocation.getLatitude() + " long: " + mLastLocation.getLongitude() + " distance from dest in miles: " + mDistancetoDestMiles);
    }

    //for share action provider
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
            Log.v(TAG, "setting share intent");
        } else {
            Log.v(TAG, "share action provider is null");
        }

        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I am currently " + mDistancetoDestMiles + " mi away from the " + mDestination);

        return shareIntent;
    }
}
