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
        super(subName, null, false);
        this.er = er;
        this.subName = subName;
    }

    @Override
    public Event test(InputChangeEvent ie, boolean override) throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public Event test() throws PhidgetException {
        return er.test(subName);
    }

    @Override
    public type getType() {
        return type.BUNDLE;
    }
}
