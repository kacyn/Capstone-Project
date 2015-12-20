package example.kacyn.com.caltrainplus;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private boolean mPermissionDenied = false;

    StationDbHelper mDbHelper;

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

        mDbHelper = new StationDbHelper(this);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
                getLoaderManager().initLoader(STATION_LOADER, null, this);
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

            Log.v(TAG, "lat: " + lat + " long: " + lng + " stationName: " + stationName);

            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(stationName));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

//    //make a separate asynctask to load the markers in a background thread
//    public class LoadMarkers extends AsyncTask<Void, Void, Void> {
//
//        private Context mContext;
//
//        public LoadMarkers (Context context) {
//            mContext = context;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            LatLng latLng = new LatLng(37.443777, -122.1649696);
//            IconGenerator iconGenerator = new IconGenerator(mContext);
//            Bitmap iconBitmap = iconGenerator.makeIcon("Palo Alto");
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//        }
//    }

}
