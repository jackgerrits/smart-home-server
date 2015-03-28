package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.jackgerrits.Sensor;
import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.NoSuchElementException;

/**
 * Created by Jack on 28/03/2015.
 */
public class ThresholdEventRule extends EventRule {

    private String name_lt;
    private String name_gt;
    private String description_lt;
    private String description_gt;
    private String sensorName;
    private int threshold;
    private double runningAverage;
    private SensorController sensorController;
    private state currentState;

    private enum state {
        INIT, LT, GT
    }

    public ThresholdEventRule(String name_lt, String name_gt, String d_lt, String d_gt, String sensorName, int val, SensorController sc, Options ops){
        super(ops);
        this.name_lt = name_lt;
        this.name_gt = name_gt;
        this.description_lt = d_lt;
        this.description_gt = d_gt;
        this.sensorName = sensorName;
        this.threshold = val;
        this.sensorController = sc;
        this.currentState = state.INIT;
        runningAverage = 0;
    }

    //this rule does not make sense for a digital input, no point testing
    public Event test(InputChangeEvent se, boolean override) throws PhidgetException {
        return null;
    }

    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
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
            //uses running average weighted more heavily on fast to reduce the impact of momentary spikes on output
            runningAverage = runningAverage*0.6 + currentValue*0.4;
            if((runningAverage > threshold) && (currentState != state.GT)){
               if(canFire() || override){
                    currentState = state.GT;
                    return new Event(name_gt,description_gt);
                }
            } else if ((runningAverage < threshold) && (currentState != state.LT)){
                if(canFire() || override){
                    currentState = state.LT;
                    return new Event(name_lt,description_lt);
                }
            }
        }
        return null;
    }

    @Override
    public Event test() throws PhidgetException {
        return null;
    }

    @Override
    public type getType() {
        return type.THRESHOLD;
    }

}
