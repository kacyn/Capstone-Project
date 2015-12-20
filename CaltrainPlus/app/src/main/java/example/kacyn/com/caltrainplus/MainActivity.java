package example.kacyn.com.caltrainplus;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String TAG = MainActivity.class.getSimpleName();

    private static final int STATION_LOADER = 0;

    private static String[] STATION_COLUMNS = {
            StationEntry._ID,
            StationEntry.COLUMN_STATION_NAME
    };

    static final int COL_STATION_ID = 0;
    static final int COL_STATION_NAME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.maps_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "you clicked the button");
//                startActivity(new Intent(MainActivity.class, MapsActivity.class));
            }
        });

//        loadStationData(this);
        new FetchStationData().execute();

        getLoaderManager().initLoader(STATION_LOADER, null, this);
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
        SimpleCursorAdapter mSpinnerAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_item,
                cursor,
                new String[]{StationEntry.COLUMN_STATION_NAME},
                new int[]{android.R.id.text1}
                );
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner stationSpinner = (Spinner) findViewById(R.id.station_spinner);
        stationSpinner.setAdapter(mSpinnerAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            parent.getItemAtPosition(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
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

        Log.v(TAG, "in parse station data");

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

                    Log.v(TAG, "Name: " + name + " Stop code: " + code);

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


    }
}
