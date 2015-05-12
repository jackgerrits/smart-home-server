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

    /**
     * Gets the static reference to itself, otherwise creates an <code>Options</code> object.
     * @return Singleton <code>Options</code> object.
     */
    public static Options get() {
        if(self == null){
            self = new Options();
        }
        return self;
    }

    /**
     * constructs options object and loads the "options.prop" file
     */
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

    /**
     * gets the list of defined sensors for the specified Phidget number
     * @param phidgetNumber phidget number to get sensors for
     * @return ArrayList of Sensors, will be empty if no Sensors were defined
     */
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

    /**
     * Checks if the property exists in the options and is defined
     * @param property property to check
     * @return true if valid
     */
    public boolean isPropertyValid(String property){
        return (properties.containsKey(property) && !properties.getProperty(property).equals(""));
    }

    /**
     * gets ArrayList of all defined Phidgets
     * @return ArrayList of Phidgets
     */
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

    /**
     * Creates Phidget from information in options.prop
     * @param phidgetNumber number of phidget
     * @return Phidget object
     */
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

    /**
     * gets serial for specified phidget, or default if property omitted
     * @param phidgetNumber specific phidget number
     * @return serial, or default -1
     */
    public int getPhidgetSerial(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-serial")){
            return Integer.parseInt(properties.getProperty(phidgetNumber+"-phidget-serial"));
        } else {
            return -1;
        }
    }


    /**
     * gets ip for specified Phidget, or default if property omitted
     * @param phidgetNumber specific phidget number
     * @return serial, or default "localhost"
     */
    public String getPhidgetIp(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-ip")){
            return properties.getProperty(phidgetNumber+"-phidget-ip");
        } else {
            return "localhost";
        }
    }

    /**
     * gets port for specified Phidget, or default if property omitted
     * @param phidgetNumber specific Phidget number
     * @return serial, or default 5001
     */
    public int getPhidgetPort(int phidgetNumber){
        if(isPropertyValid(phidgetNumber+"-phidget-port")){
            return Integer.parseInt(properties.getProperty(phidgetNumber+"-phidget-port"));
        } else {
            return 5001;
        }
    }

    /**
     * gets application server port, or default if property omitted
     * @return port, or default 8888
     */
    public int getServerPort(){
        if(isPropertyValid("server-port")) {
            return Integer.parseInt(properties.getProperty("server-port"));
        } else {
            return 8888;
        }
    }

    /**
     * gets ssl password, or default if property omitted
     * @return ssl password, or default "password"
     */
    public String getSSLPassword(){
        if(isPropertyValid("ssl-password")) {
            return properties.getProperty("ssl-password");
        } else {
            return "password";
        }
    }

    /**
     * gets ssl keystore name, or default if omitted
     * @return ssl keystore name, or default "keystore"
     */
    public String getSSLKeystore(){
        if(isPropertyValid("ssl-keystore")) {
            return properties.getProperty("ssl-keystore");
        } else {
            return "keystore";
        }
    }

    /**
     * gets default timeout, or default if omitted
     * @return timeout, or default 2000
     */
    public int getDefaultTimeout(){
        if(isPropertyValid("default-event-timeout")) {
            return Integer.parseInt(properties.getProperty("default-event-timeout"));
        } else {
            return 2000;
        }
    }

    /**
     * get username, or default if omitted
     * @return username, default admin
     */
    public String getUsername(){
        if(isPropertyValid("username")){
            return properties.getProperty("username");
        } else {
            return "admin";
        }
    }

    /**
     * gets password, or default if omitted
     * @return hash, or default "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8" hash for "password"
     */
    public String getPasswordHash(){
        if(isPropertyValid("password-hash")){
            return properties.getProperty("password-hash");
        } else {
            return "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";
        }
    }

}
