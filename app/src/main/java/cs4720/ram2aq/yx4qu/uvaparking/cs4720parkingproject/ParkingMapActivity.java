package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ParkingMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_FINE_LOC_PERMISSION = 2;
    private WeatherMonitor theMonitor;
    public static final String PREFS_NAME = "prefsFile";
    LoadParkingDataService.LoadInfoBinder theBinder;
    boolean isBound = false;

    private String homeAddress = "";
    private double homeAddressLat = 0.0, homeAddressLong = 0.0;
    private Marker homeMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_map);
//        Intent intent = new Intent(this, LoadParkingDataService.class);
//        bindService(intent, theConnection, Context.BIND_AUTO_CREATE);
        startWeatherMonitor();

        //initialize the database. probably not the best place to do this but it didn't work in LoadParkingDataService
        try {
            DatabaseHelper mDbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Reader r = new InputStreamReader(getResources().openRawResource(
                    getResources().getIdentifier("parkinginfo",
                            "raw", getPackageName())));
            BufferedReader buffer = new BufferedReader(r);
            String line = "";
            String tableName ="parkinginfo";
            String columns = "name, desc, lat, long, permitReq, permitTypes, hasMeteredSpots, " +
                    "monS, monE, tueS, tueE, wedS, wedE, thuS, thuE, friS, friE, satS, satE, sunS, sunE";
            String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
            String str2 = ");";

            db.beginTransaction();
            Log.i("LoadInfoService", "Starting reading");
            while ((line = buffer.readLine()) != null) {
                Log.i("LoadInfoService", "reading row....");
                Log.i("LoadInfoService", line);

                StringBuilder sb = new StringBuilder(str1);
                String[] str = line.split(",");
                for (int i = 0; i<20; i++){
                    if (i==0 || i==1 || i==5){
                        sb.append( "'" + str[i] + "',");
                    }else {
                        sb.append(str[i] + ",");
                    }
                }
                //append last value without comma
                sb.append(str[20]);
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        homeAddress = settings.getString("homeAddress", "Home address not set");
        homeAddressLat = settings.getFloat("homeAddressLat", 0);
        homeAddressLong = settings.getFloat("homeAddressLong",0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startWeatherMonitor();
    }

    private void startWeatherMonitor() {
        theMonitor = new WeatherMonitor(this);
        theMonitor.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        theMonitor.cancel(true);
        if (isBound) {
            unbindService(theConnection);
            isBound = false;
        }
    }

    private class WeatherMonitor extends AsyncTask<Void, String, Void> {
        private static final String URL_BASE = "http://api.openweathermap.org/data/2.5/weather?";
        private static final String CITY_CODE = "id=4752031";
        private static final String KEY = "APPID=5bb1dd457518c5d568cae76e4e4066f6";
        private String currentWeather = "";
        private Activity parent;

        public WeatherMonitor(Activity parent) {
            this.parent = parent;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                String result = requestContent(URL_BASE+CITY_CODE+"&"+KEY);
                String display_text = "Weather Unknown";
                if (result != null) {
                    Log.d("WeatherMonitor",result);
                    int startindex = result.indexOf("description")+14;
                    int endindex = result.indexOf(',',startindex)-1;
                    if (startindex != 13) // implies description not found
                        display_text = "Current weather from openweathermap.org:\n"
                                + result.substring(startindex,endindex);
                    startindex = result.indexOf("temp")+6;
                    endindex = result.indexOf(',',startindex);
                    if (startindex != 5) {
                        double kelvin = Double.parseDouble(result.substring(startindex,endindex));
                        double fahrenheit = 1.8*(kelvin-273)+32;
                        String fahrenheit_str = String.format("%.1f",fahrenheit);
                        display_text += " " + fahrenheit_str + "\u2109";
                    }
                    startindex = result.indexOf("dt")+4;
                    endindex = result.indexOf(',',startindex);
                    if (startindex != 3) {
                        Long ms = Long.valueOf(result.substring(startindex,endindex))*1000;
                        Date weatherdate = new Date(ms);
                        display_text += " " + (new SimpleDateFormat("hh:mma").format(weatherdate));
                    }
                }
                publishProgress(display_text);
                SystemClock.sleep(60000);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... s) {
            currentWeather = s[0];
            TextView weather_text = (TextView) parent.findViewById(R.id.weather_text);
            weather_text.setText(currentWeather);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap = googleMap;

        // import markers from KML file
        try {
            KmlLayer layer = new KmlLayer(mMap, R.raw.uva_parking_locator, getApplicationContext());
            layer.addLayerToMap();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // add home address marker
        if (homeAddressLat != 0 && homeAddressLong !=0) {
            if (homeMarker != null) homeMarker.remove();
            homeMarker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(homeAddressLat, homeAddressLong))
                    .title("Home")
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        //center on UVa
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.03639,-78.50754),14));

        // enable MyLocationLayer
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOC_PERMISSION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOC_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                } else {
                    Log.d("enableMyLocation","User did not grant permission to use location.");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void goToSettings(View view)
    {
        Intent intent = new Intent(ParkingMapActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void testDatabase(View view){
        Log.i("DBData", "test db called!!");

        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String[] projection = {
                "permitTypes",
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

//        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            String currID = cursor.getString(
                    cursor.getColumnIndexOrThrow("permitTypes")
            );
            Log.i("DBData", currID);
        }
    }
    // utility functions for making web request
    public String requestContent(String urlStr) {
        String result = null;
        HttpURLConnection urlConnection;
        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = convertStreamToString(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    private ServiceConnection theConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            theBinder = (LoadParkingDataService.LoadInfoBinder) service;
            isBound = true;
            Log.i("from service connection", "service was connected");
//            theAdapter.update_objects(theBinder.getTitles());
//            theAdapter.notifyDataSetChanged(); // force update check boxes
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}
