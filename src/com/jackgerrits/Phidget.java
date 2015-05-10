package com.jackgerrits;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.util.ArrayList;

/**
 * @author Jack Gerrits
 * Created by Jack on 23/04/2015.
 */
public class Phidget {
    private ArrayList<Sensor> sensors;
    private SensorController sensorController = SensorController.get();
    private InterfaceKitPhidget ik;
    private String ip = null;
    private int port = -1;
    private int serial = -1;

    public boolean isSamePhidget(com.phidgets.Phidget phidget){
        return ik == phidget;
    }

    // Used for USB
    public Phidget(ArrayList<Sensor> sensors) throws PhidgetException {
        this.sensors = sensors;

        ik = new InterfaceKitPhidget();
        ik.openAny();
        System.out.println("Attempting to connect to Phidget... [ USB ]");
        ik.waitForAttachment();
        attachListeners();
        System.out.println("Successfully connected to Phidget!");
    }

    //Specific USB using serial
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

    //Specific phidget at network address
    public Phidget(int serial, String ip, int port, ArrayList<Sensor> sensors) throws PhidgetException {
        this.serial = serial;
        this.ip = ip;
        this.port = port;
        this.sensors = sensors;

        ik = new InterfaceKitPhidget();
        ik.open(serial, ip, port);
        System.out.println("Attempting to connect to Phidget... [ NETWORK: "+ ip + ", "+ port + ", "+serial+" ]");
        ik.waitForAttachment();
        attachListeners();
        System.out.println("Successfully connected to Phidget!");
    }

    //any phidget at network address
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

    public void attachListener(SensorChangeListener scl){
        ik.addSensorChangeListener(scl);
    }

    public void removeListener(SensorChangeListener scl){
        ik.removeSensorChangeListener(scl);
    }

    public void attachListener(InputChangeListener icl){
        ik.addInputChangeListener(icl);
    }

    public void removeListener(InputChangeListener icl){
        ik.removeInputChangeListener(icl);
    }



    public ArrayList<String> getConnectedSensors(){
        ArrayList<String> res = new ArrayList<>();
        for (Sensor sensor : sensors) {
            res.add(sensor.getName());
        }
        return res;
    }

    Sensor getSensor(String sensorName) {
        for (Sensor sensor : sensors) {
            if (sensor.getName().equals(sensorName)) {
                return sensor;
            }
        }
        return null;
    }

    public Sensor getSensor(int port, Sensor.sensorType type) {
        for (Sensor sensor : sensors) {
            if ((sensor.getPort() == port) && (sensor.getType() == type)) {
                return sensor;
            }
        }
        return null;
    }

    public boolean contains(Sensor sensor){
        return sensors.contains(sensor);
    }

    public int getVal(String sensorName) throws PhidgetException {
        Sensor sensor = getSensor(sensorName);
        return getVal(sensor);
    }

    public int getVal(Sensor sensor) throws PhidgetException {
        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            return ik.getInputState(sensor.getPort()) ? 1 : 0;
        } else {
            return ik.getSensorValue(sensor.getPort());
        }
    }

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
