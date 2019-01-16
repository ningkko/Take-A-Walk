package com.example.takeawalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "PRINT";

    /**
     * activitySpinner ::= running, walking, biking
     * spinner 1 for larger unit inputs ::= hr, miles
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
            mileAdapter, mileDeciAdapter;

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
    public String activity = "running";
    public int unit1Value, unit2Value = 0;


    /**
     * position for spinner 1, unit 1
     */
    public int s1U1Position = 0;
    /**
     * position for spinner 1, unit 2
     */
    public int s2U1Position =0;

    /**
     * position for spinner 2, unit 1
     */
    public int s1U2Position = 0;
    /**
     * position for spinner 2, unit 2
     */
    public int s2U2Position =0;


    public double distance=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();


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

    }

    private void getRouteHandler() {

        // get values
        readInputs();
        speedReporter();

        // open maps page
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(Keys.DISTANCE, distance);
        intent.putExtra(Keys.ACTIVITYTYPE, activity);

        startActivity(intent);

    }

    public void changeLocationHandler(){

        Intent intent = new Intent(this, ChooseLocationActivity.class);
        startActivity(intent);
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

        mileAdapter = ArrayAdapter.createFromResource(this, R.array.mile_array, android.R.layout.simple_spinner_item);
        mileDeciAdapter = ArrayAdapter.createFromResource(this, R.array.mile_decimal_array, android.R.layout.simple_spinner_item);

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
            spinner1.setAdapter(mileAdapter);
            spinner2.setAdapter(mileDeciAdapter);
            unit1.setText(" . ");
            unit2.setText("Km");
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
