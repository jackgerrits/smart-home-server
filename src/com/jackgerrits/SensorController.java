package com.jackgerrits;

import com.jackgerrits.events.Event;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Jack on 21/03/2015.
 */
public class SensorController {
    private ArrayList<Phidget> phidgets;
    private LinkedList<Event> events;

    private Options ops;

    private EventTester eventTester;

    private static SensorController self;

    public static SensorController get(){
        if(self == null){
            self = new SensorController();
        }
        return self;
    }

    void addPhidget(Phidget phidget){
        phidgets.add(phidget);
    }

    void addPhidgets(Collection<Phidget> inPhidgets){
        phidgets.addAll(inPhidgets);
    }

    //returns the phidget that holds the queried sensor
    public Phidget getPhidget(String sensorName){
        Sensor sensor = getSensor(sensorName);
        for(Phidget p : phidgets){
            if(p.getConnectedSensors().contains(sensor.getName())){
                return  p;
            }
        }
        return null;
    }

    public SensorController(){
        self = this;
        ops = Options.get();
        phidgets = new ArrayList<>();
        events = new LinkedList<>();
        System.out.println("Loading event definitions...");
        eventTester = new EventTester();
        eventTester.loadEvents("events.json");
        System.out.println("Event definitions loaded successfully!");

        addPhidgets( ops.getPhidgets());
        checkSensorNamesUnqiue();
    }

    void processChangeEvent(SensorChangeEvent se){
        addEvents(eventTester.evalEvent(se));
    }

    void checkSensorNamesUnqiue(){
        ArrayList<String> sensors = getConnectedSensors();
        for(String s : sensors){
            for(String current : sensors ){
                if(s == current){
                    continue;
                }
                if(s.equals(current)){
                    System.out.println("OPTIONS ERROR: Sensor names must be unique - not unique: '"+ s+"'");
                    System.exit(1);
                }
            }
        }
    }

    void processChangeEvent(InputChangeEvent ie){
        addEvents(eventTester.evalEvent(ie));
    }


    public ArrayList<String> getConnectedSensors(){
        ArrayList<String> all = new ArrayList<>();
        for(Phidget p : phidgets){
           all.addAll(p.getConnectedSensors());
        }

        return all;
    }

    Sensor getSensor(String sensorName) {
        for(Phidget p : phidgets){
            Sensor current = p.getSensor(sensorName);
            if(current!=null){
                return current;
            }
        }
        return null;
    }

    public Sensor getSensor(int port, Sensor.sensorType type) {
        for(Phidget p : phidgets){
            Sensor current = p.getSensor(port, type);
            if(current!=null){
                return current;
            }
        }
        return null;
    }


    public int getVal(String sensorName) throws PhidgetException {
        for(Phidget p : phidgets){
            if(p.getSensor(sensorName)!=null){
                return p.getVal(sensorName);
            }
        }
        System.out.println("ERROR: getVal("+sensorName+") - that sensor was not found");
        return -1;
    }

    public int getVal(Sensor sensor) throws PhidgetException {
        for(Phidget p : phidgets){
            if(p.contains(sensor)){
                return p.getVal(sensor);
            }
        }
        System.out.println("ERROR: getVal("+sensor.getName()+") - that sensor was not found");
        return -1;
    }

    public boolean areEvents(){
        return !events.isEmpty();
    }

    public Event getEvent(){
        return events.remove();
    }

    public synchronized void addEvent(Event in) {
        //EventRules now handle issue of rapid firing

        addEvents(eventTester.evalEvent(in));
        if(!in.isHidden()){
            events.add(in);
        }
    }

    public void addEvents(ArrayList<Event> events)  {
        if(!events.isEmpty()) {
            for (Event event : events) {
                addEvent(event);
            }
        }
    }

    public void stop(){
        phidgets.forEach(com.jackgerrits.Phidget::stop);
    }
}
