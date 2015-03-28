package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by jackgerrits on 23/03/15.
 */
public abstract class EventRule {
    public enum type {
        EQUAL, CHANGE, THRESHOLD, AND, OR
    }

    Options ops;
    int timeout;
    long lastReturn;

    public EventRule(Options ops){
        this.ops = ops;
        timeout = ops.getEventTimeout();
        lastReturn = System.currentTimeMillis();
    }

    public boolean canFire(){
        long timeNow = System.currentTimeMillis();
        if(Math.abs(timeNow - lastReturn) > timeout){
            lastReturn = timeNow;
            return true;
        }
        return false;
    }

    public abstract Event test(InputChangeEvent ie, boolean override) throws PhidgetException;
    public abstract Event test(SensorChangeEvent se, boolean override) throws PhidgetException;
    public abstract Event test() throws PhidgetException;
    public abstract type getType();


}
