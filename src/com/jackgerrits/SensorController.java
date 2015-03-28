package com.jackgerrits;

import com.jackgerrits.events.Event;
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
    Options ops;
    String ip;
    int port;
    EventTester eventTester;

    public SensorController(String ip, int port, Options ops){
        sensors = ops.getSensors();
        this.ip = ip;
        this.port = port;
        this.ops = ops;
        events = new LinkedList<Event>();
        eventTester = new EventTester(this);
        eventTester.loadEvents("TEST");

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
                Event current = null;
                try {
                    current = eventTester.evalEvent(ie);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
                if(current != null){
                    addEvent(current);
                }
            }
        });

        ik.addSensorChangeListener(new SensorChangeListener()
        {
            @Override
            public void sensorChanged(SensorChangeEvent se) {
                Event current = null;
                try {
                    current = eventTester.evalEvent(se);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
                if(current != null){
                    addEvent(current);
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

    public int getVal(String sensorName) throws PhidgetException {
        Sensor sensor = getSensor(sensorName);
        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            System.out.println( ik.getOutputState(sensor.getPort()));
            System.out.println(sensor.getPort());
            return ik.getInputState(sensor.getPort()) ? 1 : 0;

        } else {
            return ik.getSensorValue(sensor.getPort());
        }
    }

    public int getVal(Sensor sensor) throws PhidgetException {
        if(sensor.getType() == Sensor.sensorType.DIGITAL){
            System.out.println( ik.getOutputState(sensor.getPort()));
            System.out.println(sensor.getPort());
            return ik.getInputState(sensor.getPort()) ? 1 : 0;

        } else {
            return ik.getSensorValue(sensor.getPort());
        }
    }

    public boolean areEvents(){
        return !events.isEmpty();
    }

    public void addEvent(Event in){
        //Many events are fired for one real life event, this helps reduce the stutter
        if(events.isEmpty()){
            events.add(in);
        } else {
            //only add event if it isnt within 2 seconds of last event from same sensor
            if((Math.abs(events.getLast().getTime() - in.getTime()) > ops.getEventTimeout()) && events.getLast().getName() == in.getName()){
                events.add(in);
            }
        }
    }

    public Event getEvent(){
        return events.remove();
    }

    public void stop(){
        try {
            ik.close();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
    }
}
