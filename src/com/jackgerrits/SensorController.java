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
    String ip;
    int port;

    public SensorController(ArrayList<Sensor> sensors){
        this.sensors = sensors;
        ip = utils.getPhidgetIp();
        port = utils.getPhidgetPort();
        events = new LinkedList<Event>();

        try {
            ik = new InterfaceKitPhidget();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }


        try {
            ik.openAny(ip,port);
        } catch (PhidgetException e) {
            System.out.println(e.getDescription());
            System.exit(1);
        }

        System.out.println("Attempting to connect to Phidget... [ "+ ip + ", "+ port + " ]");
        try {
            ik.waitForAttachment();
        } catch (PhidgetException e) {
            System.out.println(e.getDescription());
            System.out.println("Phidget cannot be found at that ip/port");
            System.exit(1);
        }
        System.out.println("Successfully connected to Phidget!");

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
                if(se.getIndex() == getSensor("touch").getPort()){
                    System.out.println("touch event fired!");
                    addEvent(new Event("touch", "Touch sensor touched!"));

                }
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
            try {
                System.out.println( ik.getOutputState(sensor.getPort()));
                System.out.println(sensor.getPort());
                return ik.getInputState(sensor.getPort()) ? 1 : 0;
            } catch (PhidgetException e) {
                e.printStackTrace();
            }

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

    public void addEvent(Event in){
        //Many events are fired for one real life event, this helps reduce the stutter
        if(events.isEmpty()){
            events.add(in);
        } else {
            //System.out.println(Math.abs(events.getLast().getTime() - in.getTime()));
            if((Math.abs(events.getLast().getTime() - in.getTime()) > 2000) && events.getLast().getName() == in.getName()){
                events.add(in);
            }
        }
    }

    public Event getEvent(){
        return events.remove();
    }
}
