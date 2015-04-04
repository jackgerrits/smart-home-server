package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by Jack on 28/03/2015.
 */
public class AndEventRule extends EventRule {

    private String description;
    private EventRule rule1;
    private EventRule rule2;
    private SensorController sensorController;

    /*
     * AND event works for CHANGE, EQUAL, THRESHOLD
     * For testing whether event causes threshold to trip, standard threshold events can be added.
     * To test whether state is in either of two threshold states, ThreshBundleEventRule must be passed containing the Threshold event and sub event name
     */

    public AndEventRule(String name, String description, EventRule r1, EventRule r2, SensorController sc, Options ops, boolean hideFromFeed){
        super(name, ops, hideFromFeed);
        this.description = description;
        rule1 = r1;
        rule2 = r2;
        sensorController = sc;
    }


    /*redundant code*/
    @Override
    public Event test(InputChangeEvent ie, boolean override) throws PhidgetException {
        Event res1 = rule1.test(ie, true);
        Event res2 = rule2.test(ie, true);

        if(res1 != null && (rule2.test() != null) ){
            if(canFire() || override){
                return new Event(name, description, hideFromFeed);
            }
        }
        if ((res2 != null) && (rule1.test() != null)){
            if(canFire() || override){
                return new Event(name, description, hideFromFeed);
            }
        }

        if (res1 != null && res2 != null){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }

        return null;
    }

    public Event test(Event event) throws PhidgetException {
        if(rule1.isCorrespondingTo(event)){
            if(rule2.test()!=null){
                return new Event(name, description, hideFromFeed);
            }
        }

        if(rule2.isCorrespondingTo(event)){
            if(rule1.test() != null){
                return new Event(name, description, hideFromFeed);
            }
        }
        return null;
    }

    /*redundant code*/
    @Override
    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        Event res1 = rule1.test(se, true);
        Event res2 = rule2.test(se, true);

        if((res1 != null) && (rule2.test() != null) ){
            if(override || canFire() ){
                return new Event(name, description, hideFromFeed);
            }
        }
        if ((res2 != null) && (rule1.test() != null)){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }

        if (res1 != null && res2 != null){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }

        return null;
    }

    @Override
    public Event test() throws PhidgetException {
        if((rule1.test() != null) && (rule2.test() != null)){
            return new Event(name, description, hideFromFeed);
        }
        return null;
    }

    @Override
    public type getType() {
        return type.AND;
    }
}
