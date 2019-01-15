package com.example.takeawalk;

import android.util.Log;

public class Monitor {

    /**
     * get distance, time...
     */
    public static void convertInput(){

        getDefaultSpeed();

        if (Data.inputType==2){
            Data.distance=Data.unit1Value+((double)Data.unit2Value)/10;
            Data.time = (int)(Data.distance*1000/Data.speed/60);
        }
        else{
            Data.time = Data.unit1Value*60+Data.unit2Value;
            Data.distance = Data.speed*Data.time*60/1000;
        }
    }

    //uniform speed
    public static void getDefaultSpeed(){

        if (Data.activity.toLowerCase().equals("running")){

            Data.speed=2.5;
        }else if(Data.activity.toLowerCase().equals("walking")){
            Data.speed=1.4;
        }else{
            Data.speed=5.36;
        }
    }

    public static void speedReporter(){

        Log.i(Data.TAG,"Activity type: "+Data.activity);
        Log.i(Data.TAG,"Estimated Distance to go: "+Data.distance+" km");
        Log.i(Data.TAG,"Will finish in: "+(int)(Data.time)+" minutes");
        Log.i(Data.TAG,"Estimated speed: "+Data.speed+" m/s");

    }

    public static void upDateDistanceAndTime(){

        /**
         *   @TODO
         *   update based on location.getSpeed()?
         */

    }
}
