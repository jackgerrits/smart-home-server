package com.jackgerrits;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.util.ArrayList  ;

/**
 * Created by Jack on 23/04/2015.
 */
public class Phidget {

    private ArrayList<Sensor> sensors;
    private SensorController sensorController = SensorController.get();
    private InterfaceKitPhidget ik;
    private String ip = null;
    private int port = -1;
    private int serial = -1;

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

    public ArrayList<String> getConnectedSensors(){
        ArrayList<String> res = new ArrayList<>();
        for (int i = 0; i < sensors.size(); i++) {
            res.add(sensors.get(i).getName());
        }
        return res;
    }

    Sensor getSensor(String sensorName) {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).getName().equals(sensorName)){
                return sensors.get(i);
            }
        }
        return null;
    }

    public Sensor getSensor(int port, Sensor.sensorType type) {
        for (int i = 0; i < sensors.size(); i++) {
            if ((sensors.get(i).getPort() == port) && (sensors.get(i).getType() == type)){
                return sensors.get(i);
            }
        }
        return null;
    }

    public boolean contains(Sensor sensor){
        return sensors.contains(sensor);
    }

    public int getVal(String sensorName) throws PhidgetException {
        Sensor sensor = getSensor(sensorName);
        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            return ik.getInputState(sensor.getPort()) ? 1 : 0;
        } else {
            return ik.getSensorValue(sensor.getPort());
        }
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
