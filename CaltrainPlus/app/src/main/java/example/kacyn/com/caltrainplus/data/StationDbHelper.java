package example.kacyn.com.caltrainplus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import example.kacyn.com.caltrainplus.data.StationContract.StationEntry;

/**
 * Created by kacyn on 12/19/15.
 */
public class StationDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "station.db";

    public StationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_STATION_TABLE = "CREATE TABLE " + StationEntry.TABLE_NAME + " (" +
                StationEntry._ID + " INTEGER PRIMARY KEY, " +
                StationEntry.COLUMN_STATION_NAME + " TEXT UNIQUE NOT NULL, " +
                StationEntry.COLUMN_STATION_CODE + " INTEGER UNIQUE NOT NULL, " +
                StationEntry.COLUMN_STATION_LAT + " REAL UNIQUE NOT NULL, " +
                StationEntry.COLUMN_STATION_LNG + " REAL UNIQUE NOT NULL " +
                ");";

        db.execSQL(SQL_CREATE_STATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StationEntry.TABLE_NAME);
        onCreate(db);
    }
}
