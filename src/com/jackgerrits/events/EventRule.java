package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by jackgerrits on 23/03/15.
 */
public abstract class EventRule {
    String name;
    Options ops;
    SensorController sensorController;
    int timeout;
    long lastReturn;
    boolean hideFromFeed;

    /**
     * Constructs EventRule object, provides generic functionality for subclasses
     * @param name name of event
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
    public EventRule(String name, boolean hideFromFeed, int timeout){
        this.name = name;
        ops = Options.get();

        this.timeout = timeout;
        if(timeout == -1){
            this.timeout = ops.getDefaultTimeout();
        }

        lastReturn = System.currentTimeMillis();
        this.hideFromFeed = hideFromFeed;
        this.sensorController = SensorController.get();
    }

    /**
     * Test if an event corresponds to an event rule
     * @param event event to test
     * @return true if it does
     */
    public boolean isCorrespondingTo(Event event){
        return isCorrespondingTo(event.getName());
    }

    /**
     * each subclass has a different way of determining if an event is corresponding
     * @param eventName name of event to test
     * @return true if it does
     */
    public abstract boolean isCorrespondingTo(String eventName);

    /**
     * Tests if the event can fire determined by the event timeout
     * @return true if it can fire
     */
    public boolean canFire(){
        long timeNow = System.currentTimeMillis();
        if(Math.abs(timeNow - lastReturn) > timeout){
            lastReturn = timeNow;
            return true;
        }
        return false;
    }

    /**
     * gets name of events generated from this event rule
     * @return event name
     */
    public String getName(){
        return name;
    }

    /**
     * Tests an input change event to see if this event happened or not
     * @param ie InputChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if event occurred, or null if there was no event determined
     */
    public abstract Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException;

    /**
     * Tests an sensor change event to see if this event happened or not
     * @param se SensorChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if event occurred, or null if there was no event determined
     */
    public abstract Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException;

    /**
     * Tests the current state of sensors to determine if the eventRule is currently true
     * @return Event if it is currently true
     */
    public abstract Event testEvent() throws PhidgetException;


}
