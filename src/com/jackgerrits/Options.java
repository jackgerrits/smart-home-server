package com.jackgerrits;

import com.phidgets.PhidgetException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Jack on 21/03/2015.
 */
public class Options {
    private static Options self = new Options();
    private String filename = "options.prop";
    private Properties properties;


    public static Options get() {
        if(self == null){
            self = new Options();
        }
        return self;
    }

    public Options(){
        self = this;
        properties = new Properties();

        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println(filename + " not found");
            System.exit(1);
        }
        System.out.println("Reading options file...");
        try {
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Options read successfully!");
    }

    public ArrayList<Sensor> getSensors(int phidgetNumber) {
        ArrayList<Sensor> sensors = new ArrayList<>();

        //get connected analog sensors
        for(int i =0; i < 8; i++){
            if(isPropertyValid(phidgetNumber+"-analog"+i)){
                sensors.add(new Sensor(properties.getProperty(phidgetNumber+"-analog"+i), i, Sensor.sensorType.ANALOG));
            }
        }

        //get digital sensors
        for(int i =0; i < 8; i++){
            if(isPropertyValid(phidgetNumber+"-digital"+i)){
                sensors.add(new Sensor(properties.getProperty(phidgetNumber+"-digital"+i), i, Sensor.sensorType.DIGITAL));
            }
        }

        return sensors;
    }

    public boolean isPropertyValid(String property){
        return (properties.containsKey(property) && !properties.getProperty(property).equals(""));
    }

    ArrayList<Phidget> getPhidgets(){
        if(!isPropertyValid("number-of-phidgets")){
            System.out.println("OPTIONS ERROR: 'number-of-phidgets' must be defined");
            System.exit(1);
        }
        int number = Integer.parseInt(properties.getProperty("number-of-phidgets"));
        ArrayList<Phidget> res =  new ArrayList<>();

        for(int i = 0; i < number; i++ ){
            try {
                res.add(getPhidget(i));
            } catch (PhidgetException e) {
                System.out.println("PHIDGET ERROR:");
                System.out.println(e.getDescription());
                System.exit(1);
            }
        }

     return res;
    }

    Phidget getPhidget(int phidgetNumber) throws PhidgetException{
        if(!isPropertyValid(phidgetNumber+"-phidget-type")){
            System.out.println("OPTIONS ERROR: '"+phidgetNumber+"-phidget-type' must be defined as either usb or network");
            System.exit(1);
        }
        String type = properties.getProperty((phidgetNumber+"-phidget-type"));
        switch (type){
            case "usb":
                if(isPropertyValid(phidgetNumber+"-phidget-serial")){
                    int serial = getPhidgetSerial(phidgetNumber);
                    return new Phidget(serial, getSensors(phidgetNumber));
                }
                return new Phidget(getSensors(phidgetNumber));

            case "network":
                String ip = getPhidgetIp(phidgetNumber);
                int port = getPhidgetPort(phidgetNumber);
                if(isPropertyValid(phidgetNumber+"-phidget-serial")){
                    int serial = getPhidgetSerial(phidgetNumber);
                    return new Phidget(serial, ip, port, getSensors(phidgetNumber));
                }
                return new Phidget(ip, port, getSensors(phidgetNumber));

            default:
                System.out.println("OPTIONS ERROR: Unexpected type '"+type+"' must be either usb or network");
                return null;
        }
    }


    public int getPhidgetSerial(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-serial")){
            return Integer.parseInt(properties.getProperty(phidgetNumber+"-phidget-serial"));
        } else {
            return -1;
        }
    }


    public String getPhidgetIp(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-ip")){
            return properties.getProperty(phidgetNumber+"-phidget-ip");
        } else {
            return "localhost";
        }
    }

    public int getPhidgetPort(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-port")){
            return Integer.parseInt(properties.getProperty(phidgetNumber+"-phidget-port"));
        } else {
            return 5001;
        }
    }

    public int getServerPort(){
        if(isPropertyValid("server-port")) {
            return Integer.parseInt(properties.getProperty("server-port"));
        } else {
            return 8888;
        }
    }

    public String getSSLPassword(){
        if(isPropertyValid("ssl-password")) {
            return properties.getProperty("ssl-password");
        } else {
            return "password";
        }
    }

    public String getSSLKeystore(){
        if(isPropertyValid("ssl-keystore")) {
            return properties.getProperty("ssl-keystore");
        } else {
            return "keystore";
        }
    }

    public int getDefaultTimeout(){
        if(isPropertyValid("default-event-timeout")) {
            return Integer.parseInt(properties.getProperty("default-event-timeout"));
        } else {
            return 2000;
        }
    }

    public String getUsername(){
        if(isPropertyValid("username")){
            return properties.getProperty("username");
        } else {
            return "admin";
        }
    }

    public String getPasswordHash(){
        if(isPropertyValid("password-hash")){
            return properties.getProperty("password-hash");
        } else {
            return "password";
        }
    }

}
