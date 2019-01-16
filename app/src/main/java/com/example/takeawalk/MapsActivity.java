package com.example.takeawalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton backButton;

    private boolean mPermissionDenied = false;
    private static final int overview = 0;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * for updating current location
     */
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;

    private static final String TAG = "print";

    public double distance;
    public String activityType;

    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // retrive data from activity 1
        Intent intent = getIntent();
        distance = intent.getDoubleExtra(Keys.DISTANCE,0.0);
        activityType = intent.getStringExtra(Keys.ACTIVITYTYPE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /**
         * go back to the input page
         */
        backButton = findViewById(R.id.more);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackButtonHandler();
            }
        });


        /**
         * get current locations
         */
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "location changed");
                if (currentLocation == null) {
                    currentLocation = location;
                    drawRoute();
                }

                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // checks if have user permission for getting current locations
        checkPermission();
    }


    public void checkPermission(){

        // if apk smaller than 23, no need for checking permission
        if (Build.VERSION.SDK_INT<23){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }else{

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }
    }


    /**
     * switch back to mainActivity rather than just "go back"
     */
    public void BackButtonHandler(){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        setupGoogleMapScreenSettings(googleMap);


    }

    private void drawRoute() {
        // get map with three points that make up triangular route
        Log.d(TAG, "getting route points");
        HashMap<String, double[]> routeMap = getRoutePoints(currentLocation.getLatitude(), currentLocation.getLongitude(), distance);

        LatLng current = new LatLng(23.63936, 68.14712);
        LatLng sydney = new LatLng(-34, 151);

        DirectionsResult dr = getDirectionsDetails("483 George St, Sydney NSW 2000, Australia","182 Church St, Parramatta NSW 2150, Australia",TravelMode.DRIVING);
        Log.d(TAG, "If result is null " + dr);
        if (dr != null) {
            addPolyline(dr, mMap);
            positionCamera(dr.routes[overview], mMap);
            addMarkersToMap(dr, mMap);
        }
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(results.routes[0].legs[0].startLocation.lat,results.routes[0].legs[0].startLocation.lng))
                .title(results.routes[0].legs[0].startAddress));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(results.routes[0].legs[0].endLocation.lat,results.routes[0].legs[0].endLocation.lng))
                .title(results.routes[0].legs[0].startAddress).snippet(getEndLocationTitle(results)));
    }

    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.google_maps_key))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Getting your current location", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        String message = Double.toString(location.getLatitude())+", "+Double.toString(location.getLongitude());
        Toast.makeText(this, "Current location:\n" + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }

        if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    private DirectionsResult getDirectionsDetails(String origin,String destination,TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            //Log.d(TAG, "ApiException " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            //Log.d(TAG, "InterruptedException " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            //Log.d(TAG, "IOException " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Gets a random point on a circle given center and radius of circle
     * @param xOfCenter double, x coordinate of center of circle
     * @param yOfCenter double, y coordinate of center of circle
     * @param radius double, radius of circle in decimal degrees
     * @return double array of size 2, y coordinate of point in first slot, x coordinate of point in second slot
     */
    private double[] findPointOnCircle(double xOfCenter, double yOfCenter, double radius){
        // get a random theta value in range 0 to 2pi
        double frac = random.nextDouble();
        double theta = frac*2*Math.PI;

        // get cartesian coordinates of point on circle (defined by randomly chosen theta and given radius)
        double xOfPoint = xOfCenter + (radius*Math.cos(theta));
        double yOfPoint = yOfCenter + (radius*Math.sin(theta));

        double[] point = {yOfPoint, xOfPoint};

        return point;
    }


    /**
     * Gets two points (B and C) which create a triangular route of approximately a certain distance, given a starting point A and desired distance
     * @param latitude double, latitude of starting point
     * @param longitude double, longitude of starting point
     * @param distanceMeters double, desired distance of route in meters
     * @return hashmap with string keys and double array values, key is "A", "B", or "C", and values are [latitude, longitude] of the point in an array of size 2
     */
    private HashMap<String, double[]> getRoutePoints(double latitude, double longitude, double distanceMeters) {
        HashMap<String, double[]> coordsMap = new HashMap<>();

        // rename as x and y for clarity/consistency
        double xOfA = longitude;
        double yOfA = latitude;

        // convert distance in meters to distance in decimal degrees (from https://stackoverflow.com/a/25237446)
        double d = distanceMeters / (111.32 * 1000 * Math.cos(latitude * (Math.PI / 180)));

        // pick a d1 in range d/(1+sqrt(2)) < d1 < d/2
        double frac = random.nextDouble();
        double lowRange = d/(1+Math.sqrt(2));
        double d1 = lowRange + (frac*((d/2)-lowRange));

        // find second point on route, B
        double[] B = findPointOnCircle(xOfA, yOfA, d1);

        // find midpoint of line from A to B
        double xOfMidAB = (xOfA+B[1])/2;
        double yOfMidAB = (yOfA+B[0])/2;

        // find second point on route, C
        double[] C = findPointOnCircle(xOfMidAB, yOfMidAB, (d1/2));

        // put latitudes and longitudes for each stop in map
        double[] coordsOfA = {latitude, longitude};
        coordsMap.put("A", coordsOfA);
        coordsMap.put("B", B);
        coordsMap.put("C", C);

        return coordsMap;
    }
}



