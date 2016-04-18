package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Jenny on 04/17/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ParkingInfo.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        //for now, just maps parking location name to permit type.
        //probably want another table mapping locations to times
        db.execSQL("drop table parkinginfo");
        db.execSQL("create table parkinginfo (name VARCHAR(255), desc VARCHAR(255), lat DOUBLE, long DOUBLE, permitReq BOOLEAN, permitTypes VARCHAR(255), hasMeteredSpots BOOLEAN,  " +
                "monS DOUBLE, monE DOUBLE, tueS DOUBLE, tueE DOUBLE, wedS DOUBLE, wedE DOUBLE, thuS DOUBLE, thuE DOUBLE, friS DOUBLE, friE DOUBLE, satS DOUBLE, satE DOUBLE, sunS DOUBLE, sunE DOUBLE)");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("delete table parkinginfo");
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



}