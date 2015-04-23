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
    private state currentState;

    private enum state {
        INIT, LT, GT
    }

    public ThresholdEventRule(String name, String name_lt, String name_gt, String d_lt, String d_gt, String sensorName, int val, boolean hideFromFeed){
        super(name, hideFromFeed);
        this.name_lt = name_lt;
        this.name_gt = name_gt;
        this.description_lt = d_lt;
        this.description_gt = d_gt;
        this.sensorName = sensorName;
        this.threshold = val;
        this.currentState = state.INIT;
        runningAverage = 0;
    }

    //this rule does not make sense for a digital input, no point testing
    public Event test(InputChangeEvent se, boolean override) {
        return null;
    }

    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor;
        eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            int currentValue = sensorController.getVal(eventSensor);
            //uses running average weighted more heavily on fast to reduce the impact of momentary spikes on output
            runningAverage = runningAverage*0.6 + currentValue*0.4;
            if((runningAverage > threshold) && (currentState != state.GT)){
               if(override || canFire()){
                    currentState = state.GT;
                    return new Event(name_gt,description_gt, hideFromFeed);
                }
            } else if ((runningAverage < threshold) && (currentState != state.LT)){
                if(override || canFire()){
                    currentState = state.LT;
                    return new Event(name_lt,description_lt, hideFromFeed);
                }
            }
        }
        return null;
    }

    @Override
    public Event test() throws PhidgetException {
        return null;
    }

    public Event test(String subname) throws PhidgetException {
        if(subname.equals(name_gt)){
            int currentValue = sensorController.getVal(sensorName);
            runningAverage = runningAverage*0.6 + currentValue*0.4;
            if((runningAverage > threshold)){
                return new Event(name_gt,description_gt, hideFromFeed);
            }
        } else if (subname.equals(name_lt)){
            int currentValue = sensorController.getVal(sensorName);
            runningAverage = runningAverage*0.6 + currentValue*0.4;
            if((runningAverage < threshold)){
                return new Event(name_lt,description_lt, hideFromFeed);
            }
        }
        return null;
    }

    @Override
    public boolean isCorrespondingTo(Event event){
        String n = event.getName();
        if(n.equals(name) || n.equals(name_lt) || n.equals(name_gt)){
            return true;
        }
        return false;
    }

    @Override
    public type getType() {
        return type.THRESHOLD;
    }

}
