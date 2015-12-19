package example.kacyn.com.caltrainplus.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kacyn on 12/19/15.
 */
public class StationContract {
    public static final String CONTENT_AUTHORITY = "example.kacyn.com.caltrainplus";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STATION = "station";

    public static final class StationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATION;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATION;

        //table name and columns
        public static final String TABLE_NAME = "station";

        public static final String COLUMN_STATION_NAME = "name";
        public static final String COLUMN_STATION_CODE = "code";
        public static final String COLUMN_STATION_LAT = "lat";
        public static final String COLUMN_STATION_LNG = "lng";

        public static Uri buildStationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static int getCodeFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

}
