package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * @author jackgerrits
 */
public class ChangeEventRule extends EventRule {

    private String description;
    private String sensorName;

    /**
     * Constructs AndEventRule object for the given parameters
     * @param name Name of the event to generate and this event rule
     * @param description Description of what the EventRule represents
     * @param sensorName name of the sensor to detect changes from
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
    public ChangeEventRule(String name, String description, String sensorName, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.sensorName = sensorName;
    }

    /**
     * Tests whether a given InputChangeEvent corresponds to this EventRules sensor
     * @param ie InputChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if the sensor matches, otherwise null
     */
    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        Sensor eventSensor;
        eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL, ie.getSource());

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            if(override || canFire()){
                return new Event(name, description, ie.getState()? 1 : 0, hideFromFeed);
            }
        }
        return null;
    }

    /**
     * Tests whether a given SensorChangeEvent corresponds to this EventRules sensor
     * @param se SensorChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if the sensor matches, otherwise null
     */
    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor;
        eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            if(override || canFire()){
                return new Event(name, description, se.getValue(), hideFromFeed);
            }
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

    /**
     * This event cannot be tested individually so it will always return null
     * @return null
     */
    @Override
    public Event testEvent() throws PhidgetException {
        return null;
    }

}
