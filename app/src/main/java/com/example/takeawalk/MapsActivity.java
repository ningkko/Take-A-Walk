package com.example.takeawalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;
import org.w3c.dom.Text;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity {

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
    private String mode;

    private static final String TAG = "print";

    public double distance;
    public String activityType;

    private Random random = new Random();

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(MapsActivity.this, "Getting your current location", Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener = new GoogleMap.OnMyLocationClickListener() {
        @Override
        public void onMyLocationClick(@NonNull Location location) {
            String message = Double.toString(location.getLatitude())+", "+Double.toString(location.getLongitude());
            Toast.makeText(MapsActivity.this, "Current location:\n" + message, Toast.LENGTH_LONG).show();
        }
    };

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Sydney and move the camera
            mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
            mMap.setOnMyLocationClickListener(onMyLocationClickListener);
            enableMyLocation();
            setupGoogleMapScreenSettings(googleMap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // retrive data from activity 1
        Intent intent = getIntent();
        distance = intent.getDoubleExtra(Keys.DISTANCE, 0.0);
        mode = intent.getStringExtra(Keys.ACTIVITYTYPE);
        activityType = intent.getStringExtra(Keys.ACTIVITYTYPE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(onMapReadyCallback);


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
                //Log.d(TAG, "location changed");
                if (currentLocation == null) {
                    currentLocation = location;
                    HashMap<String, double[]> routeMap = getRoutePoints(currentLocation.getLatitude(), currentLocation.getLongitude(), distance);
                    drawRoute(mMap, routeMap);
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


    public void checkPermission() {

        // if apk smaller than 23, no need for checking permission
        if (Build.VERSION.SDK_INT < 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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


    private void drawRoute(GoogleMap googleMap, HashMap<String, double[]> routeMap) throws NullPointerException{
        if (routeMap != null) {
            com.google.maps.model.LatLng A = new com.google.maps.model.LatLng(routeMap.get("A")[0], routeMap.get("A")[1]);
            com.google.maps.model.LatLng B = new com.google.maps.model.LatLng(routeMap.get("B")[0], routeMap.get("B")[1]);
            com.google.maps.model.LatLng C = new com.google.maps.model.LatLng(routeMap.get("C")[0], routeMap.get("C")[1]);

            setupGoogleMapScreenSettings(googleMap);
            TravelMode travelMode;

            if (mode.equals("walking")) {
                travelMode = TravelMode.WALKING;
            } else if (mode.equals("biking")) {
                travelMode = TravelMode.BICYCLING;
            } else {
                travelMode = TravelMode.WALKING;
            }

            DirectionsResult dr1 = getDirectionsDetails(A, B, travelMode);
            DirectionsResult dr2 = getDirectionsDetails(A, C, travelMode);
            DirectionsResult dr3 = getDirectionsDetails(B, C, travelMode);

            ArrayList<DirectionsResult> polylines = new ArrayList<>();
            polylines.add(dr1);
            polylines.add(dr2);
            polylines.add(dr3);

            Log.d(TAG, "current location is null");
            Log.d(TAG, "direction result is " + dr1);

            // keep track of total distance and total time
            double totalDistance = 0;
            double totalTimeSec = 0;

            for (int i = 0; i < polylines.size(); i++) {
                DirectionsResult dr = polylines.get(i);
                if (dr != null) {
                    totalDistance += polylines.get(i).routes[0].legs[0].distance.inMeters;
                    totalTimeSec += polylines.get(i).routes[0].legs[0].duration.inSeconds;
                    positionCamera(dr.routes[overview], googleMap);
                    addMarkersToMap(dr, googleMap);
                }
            }

            ArrayList<Pair<Polyline, String>> polys = addPolyline(polylines, googleMap);
            mapListener(polys, mMap);

            // display total distance and time for route
            TextView infoWindow = (TextView) findViewById(R.id.info);
            int totalTimeHr = (int)(totalTimeSec/3600);
            int totalTimeMin = (int)((totalTimeSec/60)%60);
            infoWindow.setText("Total distance: "+String.valueOf((int)totalDistance)+"m"+"\nTotal time: "+totalTimeHr+"hr "+totalTimeMin+"min");
        }
    }

    private ArrayList<Pair<Polyline, String>> addPolyline(ArrayList<DirectionsResult> results, final GoogleMap mMap) {
        ArrayList<Pair<Polyline, String>> polylines = new ArrayList<>();
        for (DirectionsResult result:results) {
            List<LatLng> decodedPath = PolyUtil.decode(result.routes[overview].overviewPolyline.getEncodedPath());
            Polyline pl = mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.RED).width(10));

            String time = result.routes[0].legs[0].duration.humanReadable;
            String distance = Long.toString(result.routes[0].legs[0].distance.inMeters);

            String title = "Distance: " + distance + " meters; Time: " + time;

            polylines.add(Pair.create(pl, title));

        }
        return polylines;
    }

    public void mapListener(final ArrayList<Pair<Polyline, String>> results, final GoogleMap mMap) {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng clickCoords) {

                double inf = Double.POSITIVE_INFINITY;
                Polyline p = null;
                String t = null;
                for (int i = 0; i < results.size(); i++) {

                    final String title = results.get(i).second;
                    Polyline pl = results.get(i).first;
                    pl.setColor(Color.RED);
                    for (LatLng polyCoords : pl.getPoints()) {

                        float[] results = new float[1];
                        Location.distanceBetween(clickCoords.latitude, clickCoords.longitude,
                                polyCoords.latitude, polyCoords.longitude, results);

                        if (results[0] < 1000) {
                            if (inf > results[0]) {
                                inf = results[0];
                                p = pl;
                                t = title;
                            }

                        }

                    }

                }

                if (inf != Double.POSITIVE_INFINITY && p != null & t != null) {
                    Drawable tr = new ColorDrawable(Color.TRANSPARENT);
                    p.setColor(Color.BLUE);
                    Log.d(TAG, "Second change polyline " + p.toString() + " to blue");
                    //pl.setVisible(true);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(clickCoords.latitude, clickCoords.longitude)).alpha(0).title(t));


//open the marker's info window
                    marker.showInfoWindow();
                    Log.e(TAG, "The second possible method @ " + clickCoords.latitude + " " + clickCoords.longitude);

                }
            }



        });
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12));
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


    private DirectionsResult getDirectionsDetails(com.google.maps.model.LatLng origin,com.google.maps.model.LatLng destination,TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now.plusHours(1))
                    .await();
        } catch (ApiException e) {
            Log.d(TAG, "ApiException " + e);
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException " + e);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.d(TAG, "IOException " + e);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Gets latitude and longitude of a point on a circle given center of circle and polar coordinates of the point
     * @param xOfCenter double, x coordinate of center of circle
     * @param yOfCenter double, y coordinate of center of circle
     * @param radius double, radius of circle in decimal degrees
     * @param theta double, angle point should be at
     * @return double array of size 2, y coordinate of point in first slot, x coordinate of point in second slot
     */
    private double[] findPointOnCircle(double xOfCenter, double yOfCenter, double radius, double theta) {
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

        // adjust distance down to account for 3 legs of trip and indirectness of streets
        double d1 = (d/5);

        // choose a random angle for point B
        double frac = random.nextDouble();
        double theta = frac*2*Math.PI;

        // find second point on route, B
        double[] B = findPointOnCircle(xOfA, yOfA, d1, theta);

        // find 90 degrees from point B for placement of point C
        theta = theta + (Math.PI/2);

        // find third point on route, C
        double[] C = findPointOnCircle(xOfA, yOfA, d1, theta);

        // put latitudes and longitudes for each stop in map
        double[] coordsOfA = {latitude, longitude};
        coordsMap.put("A", coordsOfA);
        coordsMap.put("B", B);
        coordsMap.put("C", C);

        return coordsMap;
    }
}



