package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by Jack on 28/03/2015.
 */
public class ChangeEventRule extends EventRule {

    private String description;
    private String sensorName;

    public ChangeEventRule(String name, String description, String sensorName, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.sensorName = sensorName;
    }

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

    @Override
    public Event testEvent() throws PhidgetException {
        return null;
    }

    @Override
    public type getType() {
        return type.CHANGE;
    }

}
