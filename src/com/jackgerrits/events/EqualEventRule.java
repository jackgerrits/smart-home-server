package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * @author jackgerrits
 */
public class EqualEventRule extends EventRule {

    private String description;
    private String sensorName;
    private int val;

    /**
     * Constructs AndEventRule object for the given parameters
     * @param name Name of the event to generate and this event rule
     * @param description Description of what the EventRule represents
     * @param sensorName name of the sensor to detect changes from
     * @param val specified sensor value for the event
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
    public EqualEventRule(String name, String description, String sensorName, int val, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.sensorName = sensorName;
        this.val = val;
    }

    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL, ie.getSource());

        return testForEvent(eventSensor, override);
    }

    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());

        return testForEvent(eventSensor, override);
    }

    public Event testEvent() throws PhidgetException {
        if(sensorController.getVal(sensorName) == val){
            return new Event(name, description, hideFromFeed);
        }
        return null;
    }

    /**
     * Tests whether an event name corresponds to this event rule
     * @param eventName name of event to test
     * @return true if it corresponding
     */
    @Override
    public boolean isCorrespondingTo(String eventName) {
        return eventName.equals(name);
    }

    //tests if the value of the sensor is the same as the event value, returns Event if it does or null otherwise
    private Event testForEvent(Sensor eventSensor, boolean override) throws PhidgetException{
        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            if(sensorController.getVal(eventSensor) == val){
                if(override || canFire()){
                    return new Event(name, description, hideFromFeed);
                }
            }
        }
        return null;
    }

}
