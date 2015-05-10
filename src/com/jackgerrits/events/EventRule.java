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
    public enum type {
        EQUAL, CHANGE, THRESHOLD, AND, OR, BUNDLE, ENTITY_DETECTION
    }

    String name;
    Options ops;
    SensorController sensorController;
    int timeout;
    long lastReturn;
    boolean hideFromFeed;

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

    public boolean isCorrespondingTo(Event event){
        return isCorrespondingTo(event.getName());
    }

    public abstract boolean isCorrespondingTo(String eventName);

    public boolean canFire(){
        long timeNow = System.currentTimeMillis();
        if(Math.abs(timeNow - lastReturn) > timeout){
            lastReturn = timeNow;
            return true;
        }
        return false;
    }

    public String getName(){
        return name;
    }

    public abstract Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException ;
    public abstract Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException;
    public abstract Event testEvent() throws PhidgetException;
    public abstract type getType();


}
