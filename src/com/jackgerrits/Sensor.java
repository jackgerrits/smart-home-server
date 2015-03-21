package com.jackgerrits;

/**
 * Created by Jack on 21/03/2015.
 */
public class Sensor {
    public enum sensorType {
        ANALOG, DIGITAL
    }

    private String name;
    private int port;
    private sensorType type;

    public Sensor(String name, int port, sensorType type){
        this.name = name;
        this.port = port;
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public int getPort(){
        return port;
    }

    public sensorType getType(){
        return type;
    }
}
