package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
/**
 * Created by Jenny on 04/17/16.
 */
public class LoadParkingDataService extends Service {

    IBinder theBinder;
    DatabaseHelper mDbHelper = new DatabaseHelper(this);
    SQLiteDatabase db = mDbHelper.getWritableDatabase();

    private void load_info() {
        String s = "";
        try {
            Reader r = new InputStreamReader(getResources().openRawResource(R.raw.parkingLocations));
            BufferedReader buffer = new BufferedReader(r);
            String line = "";
            String tableName ="parkingLocations";
            String columns = "name, desc, lat, long, permitReq, permitTypes, hasMeteredSpots, " +
                    "monS, monE, tueS, tueE, wedS, wedE, thuS, thuE, friS, friE, satS, satE, sunS, sunE";
            String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
            String str2 = ");";

            db.beginTransaction();
            while ((line = buffer.readLine()) != null) {
                StringBuilder sb = new StringBuilder(str1);
                String[] str = line.split(",");
                for (int i = 0; i<20; i++){
                    sb.append(str[i] + ",");
                }
                //append last value without comma
                sb.append(str[21]);
                sb.append(str2);
                db.execSQL(sb.toString());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            Log.d("LoadInfoService","Could not read from input file");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (theBinder == null) {
            theBinder = new LoadInfoBinder();
            load_info();
        }
        return theBinder;
    }

    public class LoadInfoBinder extends Binder {

        //not finished writing
        public ArrayList<String> getPermitTypes(String name ) {
            String[] projection = {
                    "permitTypes",
            };

            // How you want the results sorted in the resulting Cursor

            Cursor cursor = db.query(
                    "parkingLocations",  // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );

            //cursor.moveToFirst();
//            while(cursor.moveToNext()) {
//                String currID = cursor.getString(
//                        cursor.getColumnIndexOrThrow("compid")
//                );
//                Log.i("DBData", currID);
//            }
            return null;
        }

    }

}
