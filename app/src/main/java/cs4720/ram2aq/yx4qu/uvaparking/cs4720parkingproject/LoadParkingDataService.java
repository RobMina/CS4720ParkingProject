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
        try {
            DatabaseHelper mDbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Reader r = new InputStreamReader(getResources().openRawResource(
                    getResources().getIdentifier("parkinginfo",
                            "raw", getPackageName())));
            BufferedReader buffer = new BufferedReader(r);
            String line = "";
            String tableName ="parkinginfo";
            String columns = "name, desc, lat, long, permitReq, permitTypes, hasMeteredSpots, isCovered," +
                    "monS, monE, tueS, tueE, wedS, wedE, thuS, thuE, friS, friE, satS, satE, sunS, sunE";
            String str1 = "INSERT OR IGNORE INTO " + tableName + " (" + columns + ") values(";
            String str2 = ");";

            db.beginTransaction();
            Log.i("LoadInfoService", "Starting reading");
            while ((line = buffer.readLine()) != null) {
//                Log.i("LoadInfoService", "reading row....");
//                Log.i("LoadInfoService", line);

                StringBuilder sb = new StringBuilder(str1);
                String[] str = line.split(",");
                for (int i = 0; i<21; i++){
                    if (i==0 || i==1 || i==5){
                        sb.append( "'" + str[i] + "',");
                    }else {
                        sb.append(str[i] + ",");
                    }
                }
                //append last value without comma
                sb.append(str[21]);
                sb.append(str2);
                db.execSQL(sb.toString());
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();

            mDbHelper.close();
        } catch (Exception e) {
            Log.d("LoadInfoService",e.toString());
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

    }

}
