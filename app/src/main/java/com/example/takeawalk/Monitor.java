package com.example.takeawalk;

public class Monitor {

    /**
     * get distance, time...
     */
    public static void convertInput(){

        getSpeed();

        if (Data.inputType==2){
            Data.distance=Data.unit1Value+((double)Data.unit2Value)/10;
            Data.time = (int)(Data.distance/Data.speed);
        }
        else{
            Data.time = Data.unit1Value*60+Data.unit2Value;
            Data.distance = Data.speed*Data.time;
        }
    }

    //uniform speed
    public static void getSpeed(){

        if (Data.activity.toLowerCase().equals("running")){
            //5 miles per hour
            Data.speed=5.0/60;
        }else if(Data.activity.toLowerCase().equals("walking")){
            Data.speed=3.5/50;
        }else{
            Data.speed=13.0/50;
        }
    }
}
