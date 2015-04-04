package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.NoSuchElementException;

/**
 * Created by jackgerrits on 23/03/15.
 */
public abstract class EventRule {
    public enum type {
        EQUAL, CHANGE, THRESHOLD, AND, OR, BUNDLE
    }

    String name;
    Options ops;
    int timeout;
    long lastReturn;

    public EventRule(String name, Options ops){
        this.name = name;
        this.ops = ops;
        if(ops!=null){
            timeout = ops.getEventTimeout();
        } else {
            timeout = 1000;
        }
        lastReturn = System.currentTimeMillis();
    }

    public boolean isCorrespondingTo(Event event){
        if(event.getName().equals(name)){
            return true;
        }
        return false;
    }

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

    public abstract Event test(InputChangeEvent ie, boolean override) throws PhidgetException ;
    public abstract Event test(SensorChangeEvent se, boolean override) throws PhidgetException;
    public abstract Event test() throws PhidgetException;
    public abstract type getType();


}
