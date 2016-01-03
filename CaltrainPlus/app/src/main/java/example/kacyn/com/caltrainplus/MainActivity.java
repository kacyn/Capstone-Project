package example.kacyn.com.caltrainplus;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Vector;

import example.kacyn.com.caltrainplus.data.StationContract.StationEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public static String TAG = MainActivity.class.getSimpleName();

    private static final int STATION_LOADER = 0;

    private static String[] STATION_COLUMNS = {
            StationEntry._ID,
            StationEntry.COLUMN_STATION_NAME
    };

    static final int COL_STATION_ID = 0;
    static final int COL_STATION_NAME = 1;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    SharedPreferences mPrefs;
    Spinner mStationSpinner;
    Button mMapsButton;

    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request necessary permissions for Android 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access to enable arrival notifications.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    }

                });
                builder.show();
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mMapsButton = (Button) findViewById(R.id.maps_button);
        mMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapsActivity();
            }
        });

        //store result of notification toggle
        ToggleButton notificationButton = (ToggleButton) findViewById(R.id.notification_toggle);
        notificationButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = mPrefs.edit();

                if(isChecked) {
                    editor.putBoolean(getString(R.string.notification_key), true);
                }
                else {
                    editor.putBoolean(getString(R.string.notification_key), false);
                }

                editor.apply();
            }
        });

        mStationSpinner = (Spinner) findViewById(R.id.station_spinner);

        //set up listener for spinner selector
        if(mStationSpinner != null) {
            mStationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView selectedView = (TextView) findViewById(android.R.id.text1);

                    String selectedStation;

                    if (selectedView == null) {
                        selectedStation = mPrefs.getString(getString(R.string.station_key), "");
                    } else {
                        selectedStation = selectedView.getText().toString();
                    }

                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(getString(R.string.station_key), selectedStation);
                    editor.putInt(getString(R.string.station_position_key), position);
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        }

        boolean dataLoaded = mPrefs.getBoolean(getString(R.string.data_loaded_key), false);

        //only load station data once
        if(!dataLoaded) {
            Log.v(TAG, "data not loaded, calling fetch station data");

            new FetchStationData().execute();
        }

        getLoaderManager().initLoader(STATION_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean dataLoaded = mPrefs.getBoolean(getString(R.string.data_loaded_key), false);

        //only load station data once
        if(!dataLoaded) {
            new FetchStationData().execute();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (!PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
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

    void startMapsActivity() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //enable button if the cursor isn't empty
        if(cursor.getCount() > 0) {
            mMapsButton.setEnabled(true);
        }
        //if the cursor is empty, create an error toast
        else {
            Toast.makeText(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT).show();
        }

        //populate spinner
        SimpleCursorAdapter mSpinnerAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                new String[]{StationEntry.COLUMN_STATION_NAME},
                new int[]{android.R.id.text1}
                );
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mStationSpinner.setAdapter(mSpinnerAdapter);

        //retrieve selected position from shared preferences
        int selectedPos = mPrefs.getInt(getString(R.string.station_position_key), -1);

        if(selectedPos != -1) {
            mStationSpinner.setSelection(selectedPos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FetchStationData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            URL url = null;
            String resultString;

            try {
                final String STATION_BASE_URL =
                        "http://services.my511.org/Transit2.0/";
                final String STOPS_FOR_ROUTE = "GetStopsForRoute.aspx";
                final String API_PARAM_TOKEN = "token";
                final String ROUTE_IDF = "routeIDF";


                Uri uri = Uri.parse(STATION_BASE_URL).buildUpon()
                        .appendPath(STOPS_FOR_ROUTE)
                        .appendQueryParameter(API_PARAM_TOKEN, getString(R.string.api_key_511))
                        .appendQueryParameter(ROUTE_IDF, "Caltrain~LOCAL~NB")
                        .build();

                try {
                    url = new URL(uri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if(inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                resultString = buffer.toString();
                parseStationXmlResponse(resultString);
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }


            return null;
        }

    }

    void parseStationXmlResponse(String stationXmlResponse)
            throws XmlPullParserException, IOException {

        final int NUM_STATIONS = 24;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(stationXmlResponse));
        int eventType = xpp.getEventType();

        Vector<ContentValues> contentValuesVector = new Vector<>(NUM_STATIONS);

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {

                if(xpp.getName().equals("Stop")) {

                    String name = xpp.getAttributeValue("", "name");
                    int code = Integer.parseInt(xpp.getAttributeValue("", "StopCode"));

                    ContentValues stationValues = new ContentValues();
                    stationValues.put(StationEntry.COLUMN_STATION_NAME, name);
                    stationValues.put(StationEntry.COLUMN_STATION_CODE, code);
                    stationValues.put(StationEntry.COLUMN_STATION_LAT, Utility.getLatFromStopCode(code));
                    stationValues.put(StationEntry.COLUMN_STATION_LNG, Utility.getLngFromStopCode(code));

                    contentValuesVector.add(stationValues);
                }
            }
            eventType = xpp.next();
        }

        int inserted = 0;

        if (contentValuesVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(cvArray);
            inserted = this.getContentResolver().bulkInsert(StationEntry.CONTENT_URI, cvArray);
        }
        Log.v(TAG, "End document.  " + inserted + " inserted");

        //store data loaded state in shared preferences
        if (inserted == NUM_STATIONS) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(getString(R.string.data_loaded_key), true);
            editor.apply();
        }
    }
}
