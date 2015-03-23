package com.jackgerrits;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.lang.System;
import java.util.NoSuchElementException;

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
//    condition previousState;
    SensorController sensorController;

    public EventRule(String name, String description, String sensorName, int val, SensorController sensorController, condition cond){
        this.name = name;
        this.description = description;
        this.sensorName = sensorName;
        this.val = val;
        this.cond = cond;
        this.sensorController = sensorController;
//        previousState = condition.CHANGE;
    }

    public EventRule(EventRule r1, EventRule R2){
        //EVENT ONLY FIRES WHEN THE STATE SWITCHES BETWEEN THE TWO EVENTS
        //IS THIS POSSIBLE????????????
    }

    //digital
    public boolean test(InputChangeEvent ie) throws PhidgetException {
        Sensor eventSensor = null;

        try {
            eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL);
        } catch (NoSuchElementException e){
            e.printStackTrace();
            System.out.println("Port: " + ie.getIndex() + ", DIGITAL");
            System.out.println("State: " + ie.getState());
            System.out.println("--------------");
            return false;
        }

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
        Sensor eventSensor = null;
        try {
            eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);
        } catch (NoSuchElementException e){
            return false;
        }

        int curVal = 0;

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            switch (cond){
                case EQUAL:
                    if(sensorController.getVal(eventSensor) == val){
                        return true;
                    }
                    break;
                case GT:
//                    System.out.println("prev: " + previousState);
//                    System.out.println("val: " + sensorController.getVal(eventSensor));
                    //checks if the value is greater than the threshold and it isnt already in a greater than state
                    curVal = sensorController.getVal(eventSensor);
//                    if(curVal > val && previousState!=condition.GT){  // previous state is meant to stop continuous firing once it goes over threshold
                    if(curVal > val){  // previous state is meant to stop continuous firing once it goes over threshold
//                        previousState = condition.GT;
                        return true;
                    } else {
                        return false;
                    }
                case LT:
//                    System.out.println("prev: " + previousState);
//                    System.out.println("val: " + sensorController.getVal(eventSensor));
                    //checks if the value is less than the threshold and it isnt already in a less than state
                    curVal = sensorController.getVal(eventSensor);
//                    if(curVal < val && previousState!=condition.LT){ // previous state is meant to stop continuous firing once it goes under threshold
                    if(curVal < val){ // previous state is meant to stop continuous firing once it goes under threshold
//                        previousState = condition.LT;
                        return true;
                    } else {
                        return false;
                    }
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
