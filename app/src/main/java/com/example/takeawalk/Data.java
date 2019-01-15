package com.example.takeawalk;

public class Data {

    /**
     * 1 ::=time
     * 2 ::=distance
     */
    public static int inputType = 1;
    public static String activity = "running";
    public static int unit1Value, unit2Value = 0;


    /**
     * position for spinner 1, unit 1
     */
    public static int s1U1Position = 0;
    /**
     * position for spinner 1, unit 2
     */
    public static int s2U1Position =0;

    /**
     * position for spinner 2, unit 1
     */
    public static int s1U2Position = 0;
    /**
     * position for spinner 2, unit 2
     */
    public static int s2U2Position =0;

    public static String TAG = "PRINT";


    public static double distance=0;
    public static int time=0;
    public static double speed=0;
}
