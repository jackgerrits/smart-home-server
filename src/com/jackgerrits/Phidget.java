package com.jackgerrits;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.util.ArrayList;

/**
 * Object for each physical Phidget connected to the system, contains list of connected sensors and InterfaceKitPhidget for retrieving information.
 * @author Jack Gerrits
 */
public class Phidget {
    private ArrayList<Sensor> sensors;
    private SensorController sensorController = SensorController.get();
    private InterfaceKitPhidget ik;
    private String ip = null;
    private int port = -1;
    private int serial = -1;

    /**
     * Tests if Phidget's InterfaceKitPhidget is the same
     * @param phidget InterfaceKitPhidget to test
     * @return true if they refer to the same phidget
     */
    public boolean isSamePhidget(com.phidgets.Phidget phidget){
        return ik == phidget;
    }

    /**
     * Constructs Phidget connected over USB
     * @param sensors list of sensors that are connected to this Phidget
     */
    public Phidget(ArrayList<Sensor> sensors) throws PhidgetException {
        this.sensors = sensors;

        ik = new InterfaceKitPhidget();
        ik.openAny();
        System.out.println("Attempting to connect to Phidget... [ USB ]");
        ik.waitForAttachment();
        attachListeners();
        System.out.println("Successfully connected to Phidget!");
    }

    /**
     * Constructs Phidget connected over USB with specific serial number
     * @param serial serial number of Phidget
     * @param sensors list of sensors that are connected to this Phidget
     */
    public Phidget(int serial, ArrayList<Sensor> sensors) throws PhidgetException{
        this.serial = serial;
        this.sensors = sensors;

        ik = new InterfaceKitPhidget();
        ik.open(serial);
        System.out.println("Attempting to connect to Phidget... [ USB: "+ serial+" ]");
        ik.waitForAttachment();
        attachListeners();
        System.out.println("Successfully connected to Phidget!");
    }

    /**
     * Constructs Phidget connected over network, at specified address and serial number
     * @param serial serial number of Phidget
     * @param ip address of Phidget on network
     * @param port port of Phidget's webservice (default is 5001)
     * @param sensors list of sensors that are connected to this Phidget
     */
    public Phidget(int serial, String ip, int port, ArrayList<Sensor> sensors) throws PhidgetException {
        this.serial = serial;
        this.ip = ip;
        this.port = port;
        this.sensors = sensors;

        // Create new interface
        ik = new InterfaceKitPhidget();
        ik.open(serial, ip, port);
        System.out.println("Attempting to connect to Phidget... [ NETWORK: "+ ip + ", "+ port + ", "+serial+" ]");
        ik.waitForAttachment();
        attachListeners();
        System.out.println("Successfully connected to Phidget!");
    }

    /**
     * Constructs Phidget connected over network, at specified address with any address
     * @param ip address of Phidget on network
     * @param port port of Phidget's webservice (default is 5001)
     * @param sensors list of sensors that are connected to this Phidget
     */
    public Phidget(String ip, int port, ArrayList<Sensor> sensors) throws PhidgetException {
        this.ip = ip;
        this.port = port;
        this.sensors = sensors;

        ik = new InterfaceKitPhidget();
        ik.openAny(ip, port);
        System.out.println("Attempting to connect to Phidget... [ NETWORK: "+ ip + ", "+ port + " ]");
        ik.waitForAttachment();
        attachListeners();

        System.out.println("Successfully connected to Phidget!");
    }

    private void attachListeners(){
        ik.addInputChangeListener(new InputChangeListener() {
            @Override
            public void inputChanged(InputChangeEvent ie) {
                sensorController.processChangeEvent(ie);
            }
        });

        ik.addSensorChangeListener(new SensorChangeListener() {
            @Override
            public void sensorChanged(SensorChangeEvent se) {
                sensorController.processChangeEvent(se);
            }
        });
    }

    /**
     * Attach a change listener on this Phidget
     * @param scl sensorChangeListener to attach
     */
    public void attachListener(SensorChangeListener scl){
        ik.addSensorChangeListener(scl);
    }

    /**
     * Remove change listener from this Phidget
     * @param scl sensorChangeListener to remove
     */
    public void removeListener(SensorChangeListener scl){
        ik.removeSensorChangeListener(scl);
    }

    /**
     * Attach a change listener on this Phidget
     * @param icl inputChangeListener to attach
     */
    public void attachListener(InputChangeListener icl){
        ik.addInputChangeListener(icl);
    }

    /**
     * Remove change listener from this Phidget
     * @param icl inputChangeListener to remove
     */
    public void removeListener(InputChangeListener icl){
        ik.removeInputChangeListener(icl);
    }

    /**
     * Gets the string names of all connected sensors
     * @return array list of connected sensor names
     */
    public ArrayList<String> getConnectedSensors(){
        ArrayList<String> res = new ArrayList<>();
        for (Sensor sensor : sensors) {
            res.add(sensor.getName());
        }
        return res;
    }

    /**
     * Gets the sensor specified by sensorName
     * @param sensorName name of Sensor to get
     * @return Sensor, or null if no sensor found with that name
     */
    Sensor getSensor(String sensorName) {
        for (Sensor sensor : sensors) {
            if (sensor.getName().equals(sensorName)) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Gets the sensor connected at specified port and type
     * @param port port number
     * @param type ANALOG or DIGITAL
     * @return Sensor, or null if no sensor is found
     */
    public Sensor getSensor(int port, Sensor.sensorType type) {
        for (Sensor sensor : sensors) {
            if ((sensor.getPort() == port) && (sensor.getType() == type)) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Check if Phidget has the specified sensor attached
     * @param sensor Sensor to test
     * @return true if contains
     */
    public boolean contains(Sensor sensor){
        return sensors.contains(sensor);
    }

    /**
     * Gets the current value of the sensor of that name
     * @param sensorName name of sensor to get value of
     * @return current value of sensor, -1 if no sensor
     */
    public int getVal(String sensorName) throws PhidgetException {
        Sensor sensor = getSensor(sensorName);
        if(sensor!= null){
            return getVal(sensor);
        }
        return -1;
    }

    /**
     * Gets the current of the sensor
     * @param sensor sensor to get value of
     * @return current value of sensor, -1 if no sensor
     */
    public int getVal(Sensor sensor) throws PhidgetException {
        if(!contains(sensor)){
            return -1;
        }

        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            return ik.getInputState(sensor.getPort()) ? 1 : 0;
        } else {
            return ik.getSensorValue(sensor.getPort());
        }
    }

    /**
     * Closes connection to InterfaceKitPhidget
     */
    public void stop(){
        if(ik!=null){
            try {
                ik.close();
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }

    }
}
