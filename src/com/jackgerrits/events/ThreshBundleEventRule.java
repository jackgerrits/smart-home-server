package com.jackgerrits.events;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by Jack on 4/04/2015.
 */
public class ThreshBundleEventRule extends EventRule{
    ThresholdEventRule er;
    String subName;

    public ThreshBundleEventRule(ThresholdEventRule er, String subName){
        super(subName, false, 1000);
        this.er = er;
        this.subName = subName;
    }

    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public Event testEvent() throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public type getType() {
        return type.BUNDLE;
    }
}
