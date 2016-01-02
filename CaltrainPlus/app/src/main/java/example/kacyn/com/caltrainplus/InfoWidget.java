package example.kacyn.com.caltrainplus;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class InfoWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        Log.v("Map", "widget updated");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String destText = "Destination: " + prefs.getString(context.getString(R.string.station_key), "");
        String distanceText = "Distance away: " + prefs.getFloat(context.getString(R.string.distance_key), -1.0f);

//        CharSequence destText = context.getString(R.string.widget_dest);
//        CharSequence distanceText = context.getString(R.string.widget_distance);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.info_widget);
        views.setTextViewText(R.id.widget_dest_text, destText);
        views.setTextViewText(R.id.widget_distance_text, distanceText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

