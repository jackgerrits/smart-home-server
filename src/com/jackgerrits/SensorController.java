package com.jackgerrits;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Created by Jack on 21/03/2015.
 */
public class SensorController {
    ArrayList<Sensor> sensors;
    InterfaceKitPhidget ik;
    LinkedList<Event> events;

    public SensorController(ArrayList<Sensor> sensors){
        this.sensors = sensors;
        try {
            ik = new InterfaceKitPhidget();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }

        System.out.println("'" + utils.getPhidgetIp()+ "'");
        System.out.println(utils.getPhidgetPort());


        try {
            ik.openAny(utils.getPhidgetIp(),utils.getPhidgetPort());
        } catch (PhidgetException e) {
            System.out.println(e.getDescription());
            System.exit(1);
        }
        try {
            ik.waitForAttachment();
        } catch (PhidgetException e) {
            System.out.println(e.getDescription());
            System.out.println("Phidget cannot be found at that ip/port");
            System.exit(1);
        }
        System.out.println("connected");

        ik.addInputChangeListener(new InputChangeListener() {
            @Override
            public void inputChanged(InputChangeEvent ie) {
                //handle logic for processing events here
                //events.add(new Event());
            }
        });

        ik.addSensorChangeListener(new SensorChangeListener()
        {
            @Override
            public void sensorChanged(SensorChangeEvent se) {
                //handle logic for processing events here
                //events.add(new Event());
            }
        });
    }

    public String[] getConnectedSensors(){
        String[] sensorNames = new String[sensors.size()];
        for (int i = 0; i < sensors.size(); i++) {
            sensorNames[i] = sensors.get(i).getName();
        }
        return sensorNames;
    }

    public Sensor getSensor(String sensorName) throws NoSuchElementException {
        for (int i = 0; i < sensors.size(); i++) {
            if (sensors.get(i).getName().equals(sensorName)){
                return sensors.get(i);
            }
        }
        throw new NoSuchElementException();
    }

    public Sensor getSensor(int port, Sensor.sensorType type) throws NoSuchElementException {
        for (int i = 0; i < sensors.size(); i++) {
            if ((sensors.get(i).getPort() == port) && (sensors.get(i).getType() == type)){
                return sensors.get(i);
            }
        }
        throw new NoSuchElementException();
    }

    public int getVal(String sensorName){
        Sensor sensor = getSensor(sensorName);
        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            int res = -1;
            try {
                res = ik.getOutputState(sensor.getPort()) ? 1 : 0;
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
            return res;
        } else {
            try {
                return ik.getSensorValue(sensor.getPort());
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public boolean areEvents(){
        return !events.isEmpty();
    }

    public Event getEvent(){
        return events.remove();
    }
}
