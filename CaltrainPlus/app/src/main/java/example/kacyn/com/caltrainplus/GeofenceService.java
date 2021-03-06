package example.kacyn.com.caltrainplus;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by kacyn on 12/20/15.  Service to handle geofence entered events
 */
public class GeofenceService extends IntentService {

    public static final String TAG = GeofenceService.class.getSimpleName();
    SharedPreferences mPrefs;

    public GeofenceService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.v(TAG, "region entered");
            // Send notification
            boolean notificationsOn = mPrefs.getBoolean(getString(R.string.notification_key), true);

            if(notificationsOn) {
                sendNotification();
            }
        }
    }

    public void sendNotification() {

        String destination = mPrefs.getString(getString(R.string.station_key), "");

        if(!destination.equals("")){
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_directions_transit_black_24dp)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(destination);

            builder.setAutoCancel(true);

            // Get an instance of the Notification manager
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Issue the notification
            mNotificationManager.notify(0, builder.build());
        }
    }
}
