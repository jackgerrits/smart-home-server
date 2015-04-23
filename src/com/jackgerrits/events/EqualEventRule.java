package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by Jack on 28/03/2015.
 */
public class EqualEventRule extends EventRule {

    private String description;
    private String sensorName;
    private int val;

    public EqualEventRule(String name, String description, String sensorName, int val, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.sensorName = sensorName;
        this.val = val;
    }

    @Override
    public Event test(InputChangeEvent ie, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL);

        return testForEvent(eventSensor, override);
    }

    @Override
    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);

        return testForEvent(eventSensor, override);
    }

    public Event test() throws PhidgetException {
        if(sensorController.getVal(sensorName) == val){
            return new Event(name, description, hideFromFeed);
        }
        return null;
    }

    public Event testForEvent(Sensor eventSensor, boolean override) throws PhidgetException{
        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            if(sensorController.getVal(eventSensor) == val){
                if(override || canFire()){
                    return new Event(name, description, hideFromFeed);
                }
            }
        }
        return null;
    }

    @Override
    public type getType() {
        return type.EQUAL;
    }

}
