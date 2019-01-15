package com.example.takeawalk;

import android.app.Activity;
import android.content.Intent;
import android.net.IpSecManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    /**
     * activitySpinner ::= running, walking, biking
     * spinner 1 for larger unit inputs ::= hr, miles
     * spinner 2 for smaller units ::= min
     */
    private Spinner activitySpinner, spinner1,spinner2;

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
    private Button getRouteButton,changeLocationButton;


    /**
     * time? or distance?
     */
    private Button inputType;

    /**
     * units
     */
    private TextView unit1, unit2;
    private TextView startLocationLabel;


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

        inputType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputTypeHandler();
            }
        });

    }

    private void getRouteHandler() {
        // find user's current location

        // open maps page
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

        // get values
        readInputs();
        logTester();

    }


    private void initialize(){

        // setup activity drop down menu
        activitySpinner = (Spinner) findViewById(R.id.activitySpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        activityAdapter = ArrayAdapter.createFromResource(this,R.array.activity_array, android.R.layout.simple_spinner_item);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);

        hrAdapter = ArrayAdapter.createFromResource(this, R.array.hr_array, android.R.layout.simple_spinner_item);
        minAdapter = ArrayAdapter.createFromResource(this, R.array.min_array, android.R.layout.simple_spinner_item);

        mileAdapter = ArrayAdapter.createFromResource(this, R.array.mile_array, android.R.layout.simple_spinner_item);
        mileDeciAdapter = ArrayAdapter.createFromResource(this, R.array.mile_decimal_array, android.R.layout.simple_spinner_item);

        getRouteButton = (Button)findViewById(R.id.getRoute);
        changeLocationButton = (Button)findViewById(R.id.changeLocation);
        inputType = (Button) findViewById(R.id.inputType);

        unit1=(TextView) findViewById(R.id.unit1);
        unit2=(TextView) findViewById(R.id.unit2);

        startLocationLabel = (TextView) findViewById(R.id.startPositionLabel);
    }

    /**
     * switches btw DISTANCE and TIME inputs
     */
    public void changeInputTypeHandler(){

        readInputs();
        logTester();

        if (Data.inputType==2){
            Data.inputType=1;
            inputType.setText("TIME");
            spinner1.setAdapter(hrAdapter);
            spinner2.setAdapter(minAdapter);
            unit1.setText("HR");
            unit2.setText("MIN");
            spinner1.setSelection(Data.s1U1Position);
            spinner2.setSelection(Data.s2U1Position);

        }else {
            Data.inputType=2;
            inputType.setText("DISTANCE");
            spinner1.setAdapter(mileAdapter);
            spinner2.setAdapter(mileDeciAdapter);
            unit1.setText(" . ");
            unit2.setText("MI.");
            spinner1.setSelection(Data.s1U2Position);
            spinner2.setSelection(Data.s2U2Position);
        }
    }

    private void readInputs(){

        if (Data.inputType==1){
            Data.s1U1Position=spinner1.getSelectedItemPosition();
            Data.s2U1Position=spinner2.getSelectedItemPosition();
        }else {
            Data.s1U2Position = spinner1.getSelectedItemPosition();
            Data.s2U2Position = spinner2.getSelectedItemPosition();
        }

        Data.activity = activitySpinner.getSelectedItem().toString().toLowerCase();
        Data.unit1Value = Integer.parseInt(spinner1.getSelectedItem().toString());
        Data.unit2Value = Integer.parseInt(spinner2.getSelectedItem().toString());

        Monitor.convertInput();
    }


    private void logTester(){

        Log.i(Data.TAG,"Activity type: "+Data.activity);
        Log.i(Data.TAG,"Estimated Distance: "+Data.distance+" miles");
        Log.i(Data.TAG,"Will finish in: "+Data.time+" minutes");
        Log.i(Data.TAG,"Estimated speed: "+Data.speed+" based on national data");

    }
}
