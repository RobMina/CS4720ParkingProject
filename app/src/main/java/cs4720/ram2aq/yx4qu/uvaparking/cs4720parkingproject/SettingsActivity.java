package cs4720.ram2aq.yx4qu.uvaparking.cs4720parkingproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Robert on 4/17/2016.
 */
public class SettingsActivity extends Activity {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    public static final String PREFS_NAME = "prefsFile";
    private String permitType = "";
    private String permitExpDate = "";
    private EditText permitTypeView = null;
    private DatePicker permitExpDatePicker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // get existing settings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        permitType = settings.getString("uvaParkingPermitType", "");
        permitExpDate = settings.getString("uvaParkingPermitExpDate","");

        // get view references
        permitTypeView = (EditText) findViewById(R.id.permit_type_edittext);
        permitExpDatePicker = (DatePicker) findViewById(R.id.permit_expiration_datepicker);

        // update views to reflect existing settings
        if (!permitType.equals("")) permitTypeView.setText(permitType);
        if (!permitExpDate.equals("")) {
            // parse permit date
            String[] toks = permitExpDate.split("/");
            int month = Integer.parseInt(toks[0]);
            int day = Integer.parseInt(toks[1]);
            int year = Integer.parseInt(toks[2]);
            permitExpDatePicker.init(year, month, day, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // update preferences
        permitType = permitTypeView.getText().toString();
        permitExpDate = permitExpDatePicker.getMonth() + "/" + permitExpDatePicker.getDayOfMonth()
                + "/" + permitExpDatePicker.getYear();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("uvaParkingPermitType", permitType);
        editor.putString("uvaParkingPermitExpDate", permitExpDate);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void startPictureActivity(View v) {
        Intent intent = new Intent(SettingsActivity.this, PictureActivity.class);
        startActivity(intent);
    }

    //starts the get home address search
    public void getHomeAddress(View v) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) { // return from Google place autocomplete
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("AUTOCOMPLETE_RESULT", "Place: " + place.getName());
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("homeAddress", place.toString());
                editor.putFloat("homeAddressLat", (float) place.getLatLng().latitude);
                editor.putFloat("homeAddressLong", (float) place.getLatLng().longitude);
                editor.commit();
                Toast.makeText(this, "Successfully saved Home Address.", Toast.LENGTH_LONG).show();
                // add marker to map in ParkingMap onMapReady
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i("AUTOCOMPLETE_RESULT", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


}
