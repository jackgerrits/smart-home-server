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
    String filename;
    Properties properties;

    public Options(String filename){
        this.filename = filename;
        properties = new Properties();

        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println(filename + " not found");
            System.exit(1);
        }

        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Sensor> getSensors() {
        ArrayList<Sensor> sensors = new ArrayList<>();

        //get connected analog sensors
        for(int i =0; i < 8; i++){
            if(!properties.getProperty("analog"+i).equals("")){
                sensors.add(new Sensor(properties.getProperty("analog"+i), i, Sensor.sensorType.ANALOG));
            }
        }

        //get digital sensors
        for(int i =0; i < 8; i++){
            if(!properties.getProperty("digital"+i).equals("")){
                sensors.add(new Sensor(properties.getProperty("digital"+i), i, Sensor.sensorType.DIGITAL));
            }
        }

        return sensors;
    }

    public String getPhidgetIp(){
        if(properties.containsKey("phidget-ip") && !properties.getProperty("phidget-ip").equals("")){
            return properties.getProperty("phidget-ip");
        } else {
            return "localhost";
        }
    }

    public int getPhidgetPort(){
        if(properties.containsKey("phidget-port") && !properties.getProperty("phidget-port").equals("")){
            return Integer.parseInt(properties.getProperty("phidget-port"));
        } else {
            return 5001;
        }
    }

    public int getServerPort(){
        if(properties.containsKey("server-port") && !properties.getProperty("server-port").equals("")) {
            return Integer.parseInt(properties.getProperty("server-port"));
        } else {
            return 8888;
        }
    }

    public int getEventTimeout(){
        if(properties.containsKey("event-timeout") && !properties.getProperty("event-timeout").equals("")) {
            return Integer.parseInt(properties.getProperty("event-timeout"));
        } else {
            return 2000;
        }
    }

    public String getUsername(){
        if(properties.containsKey("username") && !properties.getProperty("username").equals("")){
            return properties.getProperty("username");
        } else {
            return "admin";
        }
    }

    public String getPassword(){
        if(properties.containsKey("password") && !properties.getProperty("password").equals("")){
            return properties.getProperty("password");
        } else {
            return "password";
        }
    }
}