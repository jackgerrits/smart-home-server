package com.jackgerrits.events;

import com.jackgerrits.Sensor;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * EventRule for defining behaviour above and below a threshold
 * @author jackgerrits
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

    /**
     * Constructs ThresholdEventRule object for the given parameters
     * @param name General name of this EventRule
     * @param name_lt Name of event for when it is less than the value
     * @param name_gt Name of the event for when it is greater than the value
     * @param d_lt Description of the event for when it is less than the value
     * @param d_gt Description of the event for when it is greater than the value
     * @param sensorName name of the sensor to detect changes from
     * @param val specified sensor value threshold for the event
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
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

    private state getInitialState(){
        try{
            if((sensorController.getVal(sensorName) > threshold)){
                return state.GT;
            }
        } catch (PhidgetException e){
            System.out.print(e.getDescription());

        }
        return state.LT;
    }

    /**
     * A threshold test doesn't make sense for digital inputs
     * @param ie InputChangeEvent to test
     * @param override overrides the firing timeout
     * @return null
     */
    public Event testEvent(InputChangeEvent ie, boolean override) {
        return null;
    }

    /**
     * Tests whether a given SensorChangeEvent corresponds to this EventRules sensor
     * @param se SensorChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Corresponding Event if it crosses the threshold, otherwise null
     */
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor;
        eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());

        if(eventSensor != null && eventSensor.getName().equals(sensorName)){
            int currentValue = sensorController.getVal(eventSensor);
            if((currentValue > threshold) && (currentState != state.GT)){   //changes from less than to greater than
               if(override || canFire()){
                    currentState = state.GT;
                    return new Event(name_gt,description_gt, hideFromFeed);
                }
            } else if ((currentValue < threshold) && (currentState != state.LT)){   //changes from greater than to less than
                if(override || canFire()){
                    currentState = state.LT;
                    return new Event(name_lt,description_lt, hideFromFeed);
                }
            }
        }
        return null;
    }

    /**
     * Test the current state of the ThresholdEventRule
     * @return Event corresponding to either less than or greater than
     */
    @Override
    public Event testEvent() throws PhidgetException {
        if((sensorController.getVal(sensorName) > threshold)){
            return new Event(name_gt,description_gt, hideFromFeed);
        } else {
            return new Event(name_lt,description_lt, hideFromFeed);
        }
    }

    /**
     * Tests whether an event name corresponds to this event rule
     * @param eventName name of event to test
     * @return true if it corresponding
     */
    @Override
    public boolean isCorrespondingTo(String eventName) {
        return eventName.equals(name) || eventName.equals(name_lt)  || eventName.equals(name_gt)  ;
    }

    /**
     * Tests whether an Event corresponds to this event rule
     * @param event Event to test
     * @return true if it corresponding
     */
    @Override
    public boolean isCorrespondingTo(Event event){
        String n = event.getName();
        return n.equals(name) || n.equals(name_lt) || n.equals(name_gt);
    }
}
