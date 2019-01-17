package com.example.takeawalk;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.model.DirectionsRoute;

public class ChooseLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button doneButton, cancelButton,getCurrentLocationButton;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * location the user tapped
     */
    public Location currentLocation;

    public LocationManager locationManager;
    public Location previousLocation;

    private static final int overview = 0;

    private GoogleMap mMap;



    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            Log.i("PRINT","MESSAGE:  "+
                    "onMapLongClick:\n" + latLng.latitude + " : " + latLng.longitude);

            Toast.makeText(ChooseLocationActivity.this,
                    "onMapClick:\n" + latLng.latitude + " : " + latLng.longitude,
                    Toast.LENGTH_LONG).show();
        }
    };


    private GoogleMap.OnMapLongClickListener onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            Log.i("PRINT",":  "+
                    "onMapLongClick:\n" + latLng.latitude + " : " + latLng.longitude);

            Toast.makeText(ChooseLocationActivity.this,
                    "onMapLongClick:\n" + latLng.latitude + " : " + latLng.longitude,
                    Toast.LENGTH_LONG).show();

            //Add marker on LongClick position
            MarkerOptions markerOptions =
                    new MarkerOptions().position(latLng).title(latLng.toString());
            mMap.addMarker(markerOptions);
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);


        doneButton = (Button) findViewById(R.id.done);
        cancelButton = (Button) findViewById(R.id.cancel);
        getCurrentLocationButton = (Button) findViewById(R.id.reset);

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

        getCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGetCurrentLocationButton();
            }
        });
    }


    public void setGetCurrentLocationButton(){
        Toast.makeText(this, "Getting your current location", Toast.LENGTH_SHORT).show();

    }

    public void setDoneButton() {

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        intent.putExtra(Keys.STARTLOCATION, currentLocation);
        startActivity(intent);

    }



    public void setCancelButton() {

        currentLocation = previousLocation;
        String message = Double.toString(currentLocation.getLatitude()) + ", " + Double.toString(currentLocation.getLongitude());
        Toast.makeText(this, "Reset to:\n" + message, Toast.LENGTH_LONG).show();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("QWERT","HI");
        mMap = googleMap;
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMapLongClickListener(onMapLongClickListener);
        setupGoogleMapScreenSettings(googleMap);
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