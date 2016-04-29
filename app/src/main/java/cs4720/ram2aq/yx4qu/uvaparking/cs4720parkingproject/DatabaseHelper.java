package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;


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
        db.execSQL("create table parkinginfo (name VARCHAR(255) PRIMARY KEY, desc VARCHAR(255), lat DOUBLE, long DOUBLE, permitReq BOOLEAN, permitTypes VARCHAR(255), hasMeteredSpots BOOLEAN, isCovered BOOLEAN," +
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

    //returns an array list of the permit types given a string
    public ArrayList<String> getPermitTypes(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {
                "permitTypes",
        };

        Cursor cursor = db.query(
                "parkinginfo",         // The table to query
                projection,                               // The columns to return
                "name=?",                               // The columns for the WHERE clause
                new String[]{name},                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        //cursor.moveToFirst();
        ArrayList<String> permitTypes = null;

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) { // If you use c.moveToNext() here, you will bypass the first row, which is WRONG
                String currID = cursor.getString(cursor.getColumnIndexOrThrow("permitTypes"));
                String[] str = currID.split(";");
                permitTypes = new ArrayList<String>(Arrays.asList(str));
                Log.i("DBData", currID);
            }
        }
        return permitTypes;
    }

    public String getClosestParking(double lat, double lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] projection = {
                "name",
                "lat",
                "long"
        };
        Cursor cursor = db.query(
                "parkinginfo",         // The table to query
                projection,                               // The columns to return
                null,                               // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        cursor.moveToFirst();
        String closestParking = null;
        double maxDist = Double.POSITIVE_INFINITY;
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String locName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                Double parkLat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                Double parkLong = cursor.getDouble(cursor.getColumnIndexOrThrow("long"));

                Location curLoc = new Location("curLoc");

                curLoc.setLatitude(lat);
                curLoc.setLongitude(lon);

                Location parking = new Location("parking");

                parking.setLatitude(parkLat);
                parking.setLongitude(parkLong);

                double distance = curLoc.distanceTo(parking);
                if (distance < maxDist) {
                    maxDist = distance;
                    closestParking = locName;
                }

                cursor.moveToNext();
            }
        }
        return closestParking;
    }


    //current time is between 0 and 24 in 24 hour time
    public ArrayList<MarkerOptions> getParkSpotLists(int hour, int dayofweek, String userPermitType) {
        ArrayList<MarkerOptions> parkingMarkers = new ArrayList<MarkerOptions>();
        String dayofweekstr = "";
        switch (dayofweek) {
            case 1:
                dayofweekstr = "sun";
                break;
            case 2:
                dayofweekstr = "mon";
                break;
            case 3:
                dayofweekstr = "tue";
                break;
            case 4:
                dayofweekstr = "wed";
                break;
            case 5:
                dayofweekstr = "thu";
                break;
            case 6:
                dayofweekstr = "fri";
                break;
            case 7:
                dayofweekstr = "sat";
                break;
            default:
                dayofweekstr = "";
                break;
        }
        String dayofweekpermitcolS = dayofweekstr + "S";
        String dayofweekpermitcolE = dayofweekstr + "E";

        SQLiteDatabase db = this.getWritableDatabase();

        //get parking spots that I can't park at
        String[] projection = {
                "name",
                "lat",
                "long",
                "permitTypes",
                "desc",

        };
        String whereclause = dayofweekpermitcolS + " <= ?  AND " + dayofweekpermitcolE + "> ?";
        String[] whereArgs = new String[]{
                Integer.toString(hour), Integer.toString(hour)
        };

        Cursor cursor = db.query(
                "parkinginfo",         // The table to query
                projection,                               // The columns to return
                whereclause,                               // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) { // If you use c.moveToNext() here, you will bypass the first row, which is WRONG
                String locName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String locDesc = cursor.getString(cursor.getColumnIndexOrThrow("desc"));
                Double parkLat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                Double parkLong = cursor.getDouble(cursor.getColumnIndexOrThrow("long"));
                String ptypes = cursor.getString(cursor.getColumnIndexOrThrow("permitTypes"));
                String[] ptypesarray = ptypes.split(";");
                ArrayList<String> permitTypes = new ArrayList<String>(Arrays.asList(ptypesarray));
                if (!permitTypes.contains(userPermitType) && !permitTypes.contains("any")) {
                    parkingMarkers.add(new MarkerOptions()
                            .position(new LatLng(parkLat, parkLong))
                            .title(locName)
                            .snippet(locDesc)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    Log.i("cant park at", locName);
                }
                if (permitTypes.contains(userPermitType) || permitTypes.contains("any")) {
                    parkingMarkers.add(new MarkerOptions()
                            .position(new LatLng(parkLat, parkLong))
                            .title(locName)
                            .snippet(locDesc)
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    Log.i("CAN park at", locName);
                }
                cursor.moveToNext();
            }
        }


        //get parking spots I can park at
         whereclause = dayofweekpermitcolS + " > ?  OR " + dayofweekpermitcolE + "<= ?";

        cursor = db.query(
                "parkinginfo",         // The table to query
                projection,                               // The columns to return
                whereclause,                               // The columns for the WHERE clause
                whereArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) { // If you use c.moveToNext() here, you will bypass the first row, which is WRONG
                String locName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String locDesc = cursor.getString(cursor.getColumnIndexOrThrow("desc"));
                Double parkLat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat"));
                Double parkLong = cursor.getDouble(cursor.getColumnIndexOrThrow("long"));
                String ptypes = cursor.getString(cursor.getColumnIndexOrThrow("permitTypes"));
                String[] ptypesarray = ptypes.split(";");
                parkingMarkers.add(new MarkerOptions()
                        .position(new LatLng(parkLat, parkLong))
                        .title(locName)
                        .snippet(locDesc)
                        .draggable(false)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                Log.i("CAN park at", locName);
                cursor.moveToNext();
            }
        }

        return parkingMarkers;

    }

}