package example.kacyn.com.caltrainplus.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import example.kacyn.com.caltrainplus.data.StationContract.StationEntry;

/**
 * Created by kacyn on 12/19/15.
 */
public class StationProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StationDbHelper mOpenHelper;

    static final int STATION = 100;
    static final int STATION_BY_CODE = 101;

    private static final SQLiteQueryBuilder sStationByCodeQueryBuilder;

    static {
        sStationByCodeQueryBuilder = new SQLiteQueryBuilder();

        sStationByCodeQueryBuilder.setTables(StationEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StationContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, StationContract.PATH_STATION, STATION);
        matcher.addURI(authority, StationContract.PATH_STATION + "/#", STATION_BY_CODE);

        return matcher;
    }

    private static final String sIdSelection =
            StationEntry.TABLE_NAME + "." + StationEntry._ID + " = ?";

    private static final String sCodeSelection =
            StationEntry.TABLE_NAME + "." + StationEntry.COLUMN_STATION_CODE + " = ?";

    @Override
    public boolean onCreate() {
        mOpenHelper = new StationDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            case STATION_BY_CODE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StationEntry.TABLE_NAME,
                        projection,
                        sCodeSelection,
                        new String[] {"" + StationEntry.getCodeFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
            }
            break;

            case STATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
            }
            break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STATION_BY_CODE:
                return StationEntry.CONTENT_ITEM_TYPE;
            case STATION:
                return StationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri retUri;

        switch (match) {
            case STATION: {
                long _id = db.insert(StationEntry.TABLE_NAME, null, values);

                if(_id > 0) {
                    retUri = StationEntry.buildStationUri(_id);
                }
                else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
            }
            break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return retUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        if (null == selection) selection = "1";

        switch (match) {
            case STATION:
            {
                rowsDeleted = db.delete(StationEntry.TABLE_NAME, selection, selectionArgs);
            }
            break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match) {
            case STATION:
            {
                rowsUpdated = db.update(StationEntry.TABLE_NAME, values, selection, selectionArgs);
            }
            break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;

        switch (match) {
            case STATION: {
                db.beginTransaction();
                returnCount = 0;

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StationEntry.TABLE_NAME, null, value);

                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }
}
