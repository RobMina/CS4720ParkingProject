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

    private void load_info() {
//        try {
//            DatabaseHelper mDbHelper = new DatabaseHelper(this);
//            SQLiteDatabase db = mDbHelper.getWritableDatabase();
//            //delete table if it already exists. probably not what we should do but works for now.
////            db.execSQL("drop table IF EXISTS parkinginfo");
//            Reader r = new InputStreamReader(getResources().openRawResource(
//                    getResources().getIdentifier("parkinginfo",
//                            "raw", getPackageName())));
//            BufferedReader buffer = new BufferedReader(r);
//            String line = "";
//            String tableName ="parkinginfo";
//            String columns = "name, desc, lat, long, permitReq, permitTypes, hasMeteredSpots, " +
//                    "monS, monE, tueS, tueE, wedS, wedE, thuS, thuE, friS, friE, satS, satE, sunS, sunE";
//            String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
//            String str2 = ");";
//
//            db.beginTransaction();
//            Log.i("LoadInfoService", "Starting reading");
//            while ((line = buffer.readLine()) != null) {
//                Log.i("LoadInfoService", "reading row....");
//                Log.i("LoadInfoService", line);
//
//                StringBuilder sb = new StringBuilder(str1);
//                String[] str = line.split(",");
//                for (int i = 0; i<20; i++){
//                    if (i==0 || i==1 || i==5){
//                        sb.append( "'" + str[i] + "',");
//                    }else {
//                        sb.append(str[i] + ",");
//                    }
//                }
//                //append last value without comma
//                sb.append(str[20]);
//                sb.append(str2);
//                db.execSQL(sb.toString());
//            }
//            db.setTransactionSuccessful();
//            db.endTransaction();
//            db.close();
//
//            mDbHelper.close();
//        } catch (Exception e) {
//            Log.d("LoadInfoService",e.toString());
//            Log.d("LoadInfoService","Could not read from input file");
//        }
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

        // should this be in databasehelper.java??
//        public ArrayList<String> getPermitTypes(String name) {
//            DatabaseHelper mDbHelper = new DatabaseHelper(this);
//            SQLiteDatabase db = mDbHelper.getWritableDatabase();
//            String[] projection = {
//                    "permitTypes",
//            };
//
//            Cursor cursor = db.query(
//                    "parkinginfo",         // The table to query
//                    projection,                               // The columns to return
//                    "name=?",                               // The columns for the WHERE clause
//                    new String[] { name },                            // The values for the WHERE clause
//                    null,                                     // don't group the rows
//                    null,                                     // don't filter by row groups
//                    null                                 // The sort order
//            );
//
//            //cursor.moveToFirst();
//            while(cursor.moveToNext()) {
//                String currID = cursor.getString(
//                        cursor.getColumnIndexOrThrow("permitTypes")
//                );
//                Log.i("DBData", currID);
//            }
//            return null;
//        }

    }

}
