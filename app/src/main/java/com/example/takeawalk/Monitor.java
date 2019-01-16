package com.example.takeawalk;

public class Monitor {


    /**
     * time to distance
     * @param hours
     * @param minutes
     * @param activity
     * @return
     */
    public static double convertTimeToDistanceInMeters(int hours, int minutes, String activity){

        // m/s
        double speed = getDefaultSpeedInMetersPerSec(activity);
        // time in seconds
        double time = hours*60*60+minutes*60;
        // distance in meters
        return speed*time;
    }

    public static int convertDistanceToMeters(int km, int decimalKm){

        // distance in meters
        return km*1000+decimalKm*100;

    }


    public static double getDefaultSpeedInMetersPerSec(String activity){
        if(activity.toLowerCase().equals("walking")){
            return 1.4;
        }else{
            return 5.36;
        }
    }

    public static void upDateDistanceAndTime(){

        /**
         *   @TODO
         *   update based on location.getSpeed()?
         */

    }
}
