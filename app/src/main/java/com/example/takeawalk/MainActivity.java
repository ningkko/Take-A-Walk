package com.example.takeawalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "PRINT";

    private static String startLocationChanged = "false";

    /**
     * activitySpinner ::= walking, biking
     * spinner 1 for larger unit inputs ::= hr, km
     * spinner 2 for smaller units ::= min
     */
    private Spinner activitySpinner, spinner1, spinner2;

    /**
     * activityAdapter ::= selections for activitySpinner
     * hrAdapter ::= number input for hours
     * minAdapter ::= number input for minutes*
     */
    private ArrayAdapter<CharSequence> activityAdapter,
            hrAdapter, minAdapter,
            kmAdapter, kmDeciAdapter;

    /**
     * getRouteButton ::= get recommended routes
     * changeLocationButton ::= select a new location
     */
    private Button getRouteButton, changeLocationButton;


    /**
     * time? or distance?
     */
    private Button inputTypeButton;

    /**
     * units
     */
    private TextView unit1, unit2;
    private TextView startLocationLabel;


    /**
     * 1 ::=time
     * 2 ::=distance
     */
    private static final int TIME = 1;
    private static final int DISTANCE = 2;

    public int inputType = TIME;
    public String activity = "walking";
    public int unit1Value, unit2Value = 0;


    /**
     * position for spinner 1, unit 1
     */
    public int s1U1Position = 0;
    /**
     * position for spinner 1, unit 2
     */
    public int s2U1Position = 0;

    /**
     * position for spinner 2, unit 1
     */
    public int s1U2Position = 0;
    /**
     * position for spinner 2, unit 2
     */
    public int s2U2Position = 0;

    public double distance = 0;

    public double startLatitude;
    public double startLongitude;

    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        startLocationChanged = "false";


        // Specify the layout to use when the list of choices appears
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        activitySpinner.setAdapter(activityAdapter);
        hrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(hrAdapter);
        spinner2.setAdapter(minAdapter);

        // register listener for get route button
        getRouteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getRouteHandler();
            }
        });

        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocationHandler();
            }
        });

        inputTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputTypeHandler();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "NO PERMISSION", Toast.LENGTH_SHORT).show();

            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            startLatitude = location.getLatitude();
                            startLongitude = location.getLongitude();
                            startLocationLabel.setText("Current location: ("+String.format("%.2f", location.getLatitude())+ ", "+
                                    String.format("%.2f", location.getLongitude())+ ")");

                        }
                    }
                });

    }

    private void getRouteHandler() {

        // get values
        readInputs();
        speedReporter();

        // input cannot be 0
        if (distance==0){

            Toast.makeText(this, "Hi, distance can't be 0! :D", Toast.LENGTH_SHORT).show();

        }else{

            Intent intent = new Intent(this, MapsActivity.class);

            // store distance, activity type, start longitude and latitude
            intent.putExtra(Keys.DISTANCE, distance);
            intent.putExtra(Keys.ACTIVITYTYPE, activity);
            intent.putExtra(Keys.STARTLATITUDE,startLatitude);
            intent.putExtra(Keys.STARTLONGITUDE,startLongitude);

            Log.d(TAG, "location change is " + startLocationChanged);
            intent.putExtra(Keys.LOCATION_CHANGE, startLocationChanged);

            // open maps page
            startActivity(intent);
        }

    }

    public void changeLocationHandler(){

        Intent intent = new Intent(this, ChooseLocationActivity.class);
        startActivityForResult(intent,Keys.REQUEST_STARTLOCATION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Keys.REQUEST_STARTLOCATION) {
            if (resultCode == RESULT_OK) {
                startLocationChanged = "true";
                Log.d(TAG, "start location change is " + startLocationChanged);
                startLatitude = data.getDoubleExtra(Keys.STARTLATITUDE,0.0);
                startLongitude = data.getDoubleExtra(Keys.STARTLONGITUDE,0.0);
            }
        }

        String latitude = String.format("%.2f",startLatitude);
        String longitude = String.format("%.2f",startLongitude);

        startLocationLabel.setText(String.format("(%s, %s)",latitude,longitude));

        super.onActivityResult(requestCode, resultCode, data);


    }


    private void initialize() {

        // setup activity drop down menu
        activitySpinner = (Spinner) findViewById(R.id.activitySpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        activityAdapter = ArrayAdapter.createFromResource(this, R.array.activity_array, android.R.layout.simple_spinner_item);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        hrAdapter = ArrayAdapter.createFromResource(this, R.array.hr_array, android.R.layout.simple_spinner_item);
        minAdapter = ArrayAdapter.createFromResource(this, R.array.min_array, android.R.layout.simple_spinner_item);

        kmAdapter = ArrayAdapter.createFromResource(this, R.array.mile_array, android.R.layout.simple_spinner_item);
        kmDeciAdapter = ArrayAdapter.createFromResource(this, R.array.mile_decimal_array, android.R.layout.simple_spinner_item);

        getRouteButton = (Button) findViewById(R.id.getRoute);
        changeLocationButton = (Button) findViewById(R.id.changeLocation);
        inputTypeButton = (Button) findViewById(R.id.inputType);

        unit1 = (TextView) findViewById(R.id.unit1);
        unit2 = (TextView) findViewById(R.id.unit2);

        startLocationLabel = (TextView) findViewById(R.id.startPositionLabel);

    }


    /**
     * switches btw DISTANCE and TIME inputs
     */
    public void changeInputTypeHandler(){

        readInputs();
        speedReporter();

        if (inputType==DISTANCE){
            inputType=TIME;
            inputTypeButton.setText("TIME");
            spinner1.setAdapter(hrAdapter);
            spinner2.setAdapter(minAdapter);
            unit1.setText("HR");
            unit2.setText("MIN");
            spinner1.setSelection(s1U1Position);
            spinner2.setSelection(s2U1Position);

        }else {
            inputType=DISTANCE;
            inputTypeButton.setText("DISTANCE");
            spinner1.setAdapter(kmAdapter);
            spinner2.setAdapter(kmDeciAdapter);
            unit1.setText(" . ");
            unit2.setText("km");
            spinner1.setSelection(s1U2Position);
            spinner2.setSelection(s2U2Position);
        }
    }

    private void readInputs(){

        activity = activitySpinner.getSelectedItem().toString().toLowerCase();
        // hours or km
        unit1Value = Integer.parseInt(spinner1.getSelectedItem().toString());
        // minutes or decimal km
        unit2Value = Integer.parseInt(spinner2.getSelectedItem().toString());

        if (inputType==TIME){
            s1U1Position=spinner1.getSelectedItemPosition();
            s2U1Position=spinner2.getSelectedItemPosition();
            distance=Monitor.convertTimeToDistanceInMeters(unit1Value, unit2Value, activity);
        }else {
            s1U2Position = spinner1.getSelectedItemPosition();
            s2U2Position = spinner2.getSelectedItemPosition();
            distance=Monitor.convertDistanceToMeters(unit1Value,unit2Value);
        }


    }


    public void speedReporter(){

        Log.i(TAG,"Activity type: "+activity);
        Log.i(TAG,"Estimated Distance to go: "+distance+" meters");

    }

}
