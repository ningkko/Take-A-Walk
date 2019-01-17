package com.example.takeawalk;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ChooseLocationActivity extends AppCompatActivity{

    private Button doneButton, cancelButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * location the user tapped, set to current location when initialized
     */
    public Location currentLocation = new Location(LocationManager.GPS_PROVIDER);
    public Location previousLocation = new Location(LocationManager.GPS_PROVIDER);

    /**
     * the map we're using
     */
    private GoogleMap mMap;

    /**
     * marker on the map
     */
    private Marker marker=null;


    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            //show message
            Toast.makeText(ChooseLocationActivity.this, "Getting your current location", Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {

            // update previous location and current location
            if (latLng != null) {
                previousLocation.set(currentLocation);
                currentLocation.setLatitude(latLng.latitude);
                currentLocation.setLongitude(latLng.longitude);

                // round the latitude and longitude
                String latitude = String.format("%.2f", latLng.latitude);
                String longitude = String.format("%.2f", latLng.longitude);

                // show message
                Toast.makeText(ChooseLocationActivity.this,
                        "( " + latitude + " , " + longitude + " )",
                        Toast.LENGTH_LONG).show();

                // initialize the marker
                if (marker == null) {
                    marker = mMap.addMarker(new MarkerOptions().position(latLng));
                }

                // set the marker to a new position
                marker.setPosition(latLng);
            }

            else {

                // show error message
                Toast.makeText(ChooseLocationActivity.this,
                        "CANNOT FIND LOCATION INFO",
                        Toast.LENGTH_SHORT).show();

            }
        }


    };



    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            String message = Double.toString(location.getLatitude())+", "+Double.toString(location.getLongitude());

            Log.i("PRINT","MESSAGE:  "+ message);
            Toast.makeText(ChooseLocationActivity.this, "Current location:\n" + message, Toast.LENGTH_LONG).show();

        }
    };


    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {

            mMap = googleMap;

            // register
            mMap.setOnMyLocationClickListener(onMyLocationClickListener);
            mMap.setOnMapClickListener(onMapClickListener);

            setupGoogleMapScreenSettings(googleMap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.chooseLocationMap);
        mapFragment.getMapAsync(onMapReadyCallback);

        doneButton = (Button) findViewById(R.id.done);
        cancelButton = (Button) findViewById(R.id.cancel);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoneButton();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCancelButton();
            }
        });

    }


    public void setDoneButton() {

        Intent intent = new Intent(this, MainActivity.class);

        // store latitude and longitude of the start position user selected
        intent.putExtra(Keys.STARTLATITUDE, currentLocation.getLatitude());
        intent.putExtra(Keys.STARTLONGITUDE, currentLocation.getLongitude());

        setResult(RESULT_OK,intent);
        finish();

    }



    public void setCancelButton() {

        // set current location back to previous location
        currentLocation = previousLocation;
        // generate its latitude and longitude
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLatitude());

        // initialize the marker
        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(latLng));
        }

        // set the marker to a new position
        marker.setPosition(latLng);
    }


    private void setupGoogleMapScreenSettings(GoogleMap mMap) {

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }


}