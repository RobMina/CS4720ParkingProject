package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.kml.KmlLayer;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ParkingMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_FINE_LOC_PERMISSION = 2;
    private WeatherMonitor theMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_map);
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

        // import markers from KML file
        try {
            KmlLayer layer = new KmlLayer(mMap, R.raw.uva_parking_locator, getApplicationContext());
            layer.addLayerToMap();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void addButtonClicked(View v) {
        // launch autocomplete widget
        AutocompleteFilter.Builder theFilterBlder = // only search for addresses
                new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS);
        LatLngBounds theBounds = new LatLngBounds(new LatLng(37.99413,-78.5749), new LatLng(38.10511,-78.4259)); // only search in C'ville
        PlaceAutocomplete.IntentBuilder theIntentBuilder =
                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(theFilterBlder.build()).setBoundsBias(theBounds);
        try {
            Intent intent = theIntentBuilder.build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("AUTOCOMPLETE_RESULT", "Place: " + place.getName());
                Toast.makeText(this, "Saving home address not yet implemented.", Toast.LENGTH_LONG).show();
                // add marker to map and update DB
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("AUTOCOMPLETE_RESULT", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
