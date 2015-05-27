package com.jackgerrits;

import com.jackgerrits.events.Event;
import com.jackgerrits.events.EventTester;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Controller for interfacing with collection of connected Phidgets and for handling sensor change events <br>
 * Holds event queue for event feed <br>
 * It is a singleton class
 * @author Jack Gerrits
 */
public class SensorController {
    private ArrayList<Phidget> phidgets;
    private LinkedList<Event> events;
    private Options ops;
    private EventTester eventTester;

    private static SensorController self;

    /**
     * Gets the static reference to itself, otherwise creates a <code>SensorController</code> object.
     * @return Singleton <code>SensorController</code> object.
     */
    public static SensorController get(){
        if(self == null){
            self = new SensorController();
        }
        return self;
    }

    /**
     * Constructs SensorController, loads Phidgets as specified in options.prop, loads event definitions as defined in events.json
     */
    public SensorController(){
        self = this;
        ops = Options.get();
        phidgets = ops.getPhidgets();
        checkSensorNamesUnqiue();
        events = new LinkedList<>();
        System.out.println("Loading event definitions...");
        eventTester = new EventTester();
        eventTester.loadEvents("events.json");
        System.out.println("Event definitions loaded successfully!");
    }

    /**
     * Gets the Phidget object for a sensor name
     * @param sensorName sensor name String to search for
     * @return the Phidget which the sensor is connected to, or null if sensor does not exist
     */
    public Phidget getPhidget(String sensorName){
        Sensor sensor = getSensor(sensorName);
        for(Phidget p : phidgets){
            if(p.getConnectedSensors().contains(sensor.getName())){
                return  p;
            }
        }
        return null;
    }

    
    public ArrayList<Phidget> getPhidgets(){
        ArrayList<Phidget> copied = new ArrayList<>(phidgets);  //copies new arraylist so
        return copied;
    }

    /**
     * Evaluate sensor change event, adds deduced events to event queue, for analog sensor changes.
     * @param se SensorChangeEvent to evaluate
     */
    void processChangeEvent(SensorChangeEvent se){
        addEvents(eventTester.evalEvent(se));
    }

    /**
     * Verifies that all of the sensor names are unique, stops program and notifies user if they are not
     */
    private void checkSensorNamesUnqiue(){
        ArrayList<String> sensors = getConnectedSensors();
        for(String s : sensors){
            for(String current : sensors ){
                if(s == current){
                    continue;   //if the string references are the same then we can skip over this entry
                }
                if(s.equals(current)){
                    System.out.println("OPTIONS ERROR: Sensor names must be unique - not unique: '"+ s+"'");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Evaluate sensor change event, adds deduced events to event queue, for digital sensor changes.
     * @param ie InputChangeEvent to evaluate
     */
    void processChangeEvent(InputChangeEvent ie){
        addEvents(eventTester.evalEvent(ie));
    }


    /**
     * Gets ArrayList of sensor name Strings from all connected Phidgets
     * @return list of all sensor names
     */
    public ArrayList<String> getConnectedSensors(){
        ArrayList<String> all = new ArrayList<>();
        for(Phidget p : phidgets){
           all.addAll(p.getConnectedSensors());
        }

        return all;
    }

    /**
     * gets the Sensor corresponding to sensorName
     * @param sensorName sensor name string to search for
     * @return Sensor object if found, null if not found
     */
    Sensor getSensor(String sensorName) {
        for(Phidget p : phidgets){
            Sensor current = p.getSensor(sensorName);
            if(current!=null){
                return current;
            }
        }
        return null;
    }

    /**
     * Gets Sensor corresponding to port and type
     * @param port Phidget port it is connected to
     * @param type Sensor type (ANALOG, DIGITAL)
     * @param sourcePhidget interfaceKitPhidget object which manages this phidget, retrieved from .getSource() on change event
     * @return Sensor object if found, null if not found
     */
    public Sensor getSensor(int port, Sensor.sensorType type, com.phidgets.Phidget sourcePhidget) {
        for(Phidget p : phidgets){
            Sensor current = p.getSensor(port, type);
            if(current!=null && p.isSamePhidget(sourcePhidget)){
                return current;
            }
        }
        return null;
    }


    /**
     * Gets the current value of sensor
     * @param sensorName sensor name String of sensor
     * @return value of sensor, or -1 if sensor is not found
     * @throws PhidgetException error connecting to Phidget
     */
    public int getVal(String sensorName) throws PhidgetException {
        for(Phidget p : phidgets){
            if(p.getSensor(sensorName)!=null){
                return p.getVal(sensorName);
            }
        }
        System.out.println("ERROR: getVal("+sensorName+") - that sensor was not found");
        return -1;
    }

    /**
     * Gets the current value of sensor
     * @param sensor Sensor object to get current value of
     * @return value of sensor, or -1 if sensor is not found
     * @throws PhidgetException error connecting to Phidget
     */
    public int getVal(Sensor sensor) throws PhidgetException {
        for(Phidget p : phidgets){
            if(p.contains(sensor)){
                return p.getVal(sensor);
            }
        }
        System.out.println("ERROR: getVal("+sensor.getName()+") - that sensor was not found");
        return -1;
    }

    /**
     * Evaluate the event of the given eventName string
     * @param eventName name of event as defined in events.json
     * @return Event if event is true, null if false
     */
    public Event evalEvent(String eventName){
        return eventTester.evalEvent(eventName);
    }

    /**
     * Check if there are events in the event queue
     * @return true if there are events in queue, false if empty
     */
    public boolean areEvents(){
        return !events.isEmpty();
    }

    /**
     * Get event from queue and remove
     * @return Event from queue
     */
    public Event getEvent(){
        if(areEvents()){
            return events.remove();
        } else {
            return null;
        }
    }

    /**
     * Add event to queue
     * @param in Event to add
     */
    public synchronized void addEvent(Event in) {
        //sends event off to be tested for AND events
        addEvents(eventTester.evalEvent(in));
        if(!in.isHidden()){
            events.add(in);
        }
    }

    /**
     * Add events to queue
     * @param events Events to add
     */
    public void addEvents(ArrayList<Event> events)  {
        if(!events.isEmpty()) {
            for (Event event : events) {
                addEvent(event);
            }
        }
    }

    /**
     * Disconnects from each connected Phidget
     */
    public void stop(){
        phidgets.forEach(com.jackgerrits.Phidget::stop);
    }
}
