package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

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
    private state currentState;

    private enum state {
        LT, GT
    }

    public ThresholdEventRule(String name, String name_lt, String name_gt, String d_lt, String d_gt, String sensorName, int val, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.name_lt = name_lt;
        this.name_gt = name_gt;
        this.description_lt = d_lt;
        this.description_gt = d_gt;
        this.sensorName = sensorName;
        this.threshold = val;
        this.currentState = getInitialState();
    }

    state getInitialState(){
        try{
            if((sensorController.getVal(sensorName) > threshold)){
                return state.GT;
            }
        } catch (PhidgetException e){
            System.out.print(e.getDescription());

        }
        return state.LT;
    }

    //this rule does not make sense for a digital input, no point testing
    public Event testEvent(InputChangeEvent se, boolean override) {
        return null;
    }

    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor;
        eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            int currentValue = sensorController.getVal(eventSensor);
            if((currentValue > threshold) && (currentState != state.GT)){
               if(override || canFire()){
                    currentState = state.GT;
                    return new Event(name_gt,description_gt, hideFromFeed);
                }
            } else if ((currentValue < threshold) && (currentState != state.LT)){
                if(override || canFire()){
                    currentState = state.LT;
                    return new Event(name_lt,description_lt, hideFromFeed);
                }
            }
        }
        return null;
    }

    @Override
    public Event testEvent() throws PhidgetException {
        if((sensorController.getVal(sensorName) > threshold)){
            return new Event(name_gt,description_gt, hideFromFeed);
        } else {
            return new Event(name_lt,description_lt, hideFromFeed);
        }
    }

    @Override
    public boolean isCorrespondingTo(String eventName) {
        return eventName.equals(name) || eventName.equals(name_lt)  || eventName.equals(name_gt)  ;
    }


    @Override
    public boolean isCorrespondingTo(Event event){
        String n = event.getName();
        return n.equals(name) || n.equals(name_lt) || n.equals(name_gt);
    }
}
