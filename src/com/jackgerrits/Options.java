package com.jackgerrits;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Jack on 21/03/2015.
 */
public class Options {
    public static ArrayList<Sensor> getSensors() {
        ArrayList<Sensor> sensors = new ArrayList<Sensor>();
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("options.prop");
        } catch (FileNotFoundException e) {
            System.out.println("options.prop not found");
            System.exit(1);
        }

        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get connected analog sensors
        for(int i =0; i < 8; i++){
            if(!properties.getProperty("analog"+i).equals("")){
                sensors.add(new Sensor(properties.getProperty("analog"+i), i, Sensor.sensorType.ANALOG));
            }
        }

        for(int i =0; i < 8; i++){
            if(!properties.getProperty("digital"+i).equals("")){
                sensors.add(new Sensor(properties.getProperty("digital"+i), i, Sensor.sensorType.DIGITAL));
            }
        }

        return sensors;
    }

    public static String getPhidgetIp(){
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("options.prop");
        } catch (FileNotFoundException e) {
            System.out.println("options.prop not found");
            System.exit(1);
        }

        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty("phidget-ip");
    }

    public static int getPhidgetPort(){

        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("options.prop");
        } catch (FileNotFoundException e) {
            System.out.println("options.prop not found");
            System.exit(1);
        }

        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(properties.getProperty("phidget-port"));

    }

    public static int getServerPort(){
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("options.prop");
        } catch (FileNotFoundException e) {
            System.out.println("options.prop not found");
            System.exit(1);
        }

        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(properties.getProperty("server-port"));
    }


}
