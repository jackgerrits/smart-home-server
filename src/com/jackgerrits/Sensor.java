package com.jackgerrits;

/**
 * Data structure for a sensor
 * @author jackgerrits
 */
public class Sensor {
    /**
     * Defines the two different sensor input on the Phidget, used when referencing sensors
     */
    public enum sensorType {
        ANALOG, DIGITAL
    }

    private String name;
    private int port;
    private sensorType type;


    /**
     * Constructs sensor object
     * @param name sensor name as defined in options
     * @param port port number on phidget for sensor
     * @param type analog or digital type
     */
    public Sensor(String name, int port, sensorType type){
        this.name = name;
        this.port = port;
        this.type = type;
    }

    /**
     * gets sensor name
     * @return sensor name
     */
    public String getName(){
        return name;
    }

    /**
     * gets sensor port
     * @return sensor port
     */
    public int getPort(){
        return port;
    }

    /**
     * gets sensor type
     * @return sensor type
     */
    public sensorType getType(){
        return type;
    }
}
