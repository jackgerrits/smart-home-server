package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.NoSuchElementException;

/**
 * Created by Jack on 28/03/2015.
 */
public class ThresholdEventRule implements EventRule {

    String name_lt;
    String name_gt;
    String description_lt;
    String description_gt;
    String sensorName;
    int threshold;
    SensorController sensorController;
    state currentState;

    private enum state {
        INIT, LT, GT
    }

    public ThresholdEventRule(String name_lt, String name_gt, String d_lt, String d_gt, String sensorName, int val, SensorController sc){
        this.name_lt = name_lt;
        this.name_gt = name_gt;
        this.description_lt = d_lt;
        this.description_gt = d_gt;
        this.sensorName = sensorName;
        this.threshold = val;
        this.sensorController = sc;
        this.currentState = state.INIT;
    }

    //this rule does not make sense for a digital input, no point testing
    public Event test(InputChangeEvent se) throws PhidgetException {
        return null;
    }

    public Event test(SensorChangeEvent se) throws PhidgetException {
        Sensor eventSensor;

        try {
            eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);
        } catch (NoSuchElementException e){
            e.printStackTrace();
            System.out.println("Port: " + se.getIndex() + ", DIGITAL");
            System.out.println("Value: " + se.getValue());
            System.out.println("--------------");
            return null;
        }

        if(eventSensor.getName().equals(sensorName)){
            int currentValue = sensorController.getVal(eventSensor);
            if((currentValue > threshold) && (currentState != state.GT)){
                currentState = state.GT;
                return new Event(name_gt,description_gt);

            } else if ((currentValue < threshold) && (currentState != state.LT)){
                currentState = state.LT;
                return new Event(name_lt,description_lt);
            }
        }
        return null;
    }
}
