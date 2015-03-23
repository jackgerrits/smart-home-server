package com.jackgerrits;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by jackgerrits on 23/03/15.
 */
public class EventRule {
    public enum condition {
        EQUAL, GT, LT, CHANGE
    }

    String name;
    String description;
    String sensorName;
    int val;
    condition cond;
    SensorController sensorController;

    public EventRule(String name, String description, String sensorName, int val, SensorController sensorController, condition cond){
        this.name = name;
        this.description = description;
        this.sensorName = sensorName;
        this.val = val;
        this.cond = cond;
        this.sensorController = sensorController;
    }

    //digital
    public boolean test(InputChangeEvent ie) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL);
        if(eventSensor.getName().equals(sensorName)){
            switch (cond){
                case EQUAL:
                    if(sensorController.getVal(eventSensor) == val){
                        return true;
                    }
                    break;
                case CHANGE:
                    return true;
            }
        } else {
            return false;
        }
        return false;
    }

    //analog
    public boolean test(SensorChangeEvent se) throws PhidgetException{
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);
        if(eventSensor.getName().equals(sensorName)){
            switch (cond){
                case EQUAL:
                    if(sensorController.getVal(eventSensor) == val){
                        return true;
                    }
                    break;
                case GT:
                    if(sensorController.getVal(eventSensor) > val){
                        return true;
                    }
                    break;
                case LT:
                    if(sensorController.getVal(eventSensor) < val){
                        return true;
                    }
                    break;
                case CHANGE:
                    return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public Event getEvent(){
        return new Event(name, description);
    }
}
