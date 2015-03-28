package com.jackgerrits.events;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by jackgerrits on 23/03/15.
 */
public interface EventRule {
//    public enum type {
//        EQUAL, CHANGE, THRESHOLD, AND, OR
//    }

    public abstract Event test(InputChangeEvent ie) throws PhidgetException;
    public abstract Event test(SensorChangeEvent se) throws PhidgetException;

}
