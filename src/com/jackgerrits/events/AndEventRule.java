package com.jackgerrits.events;

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

    /*
     * AND event works for CHANGE, EQUAL, THRESHOLD
     * For testing whether event causes threshold to trip, standard threshold events can be added.
     * To testEvent whether state is in either of two threshold states, ThreshBundleEventRule must be passed containing the Threshold event and sub event name
     */

    public AndEventRule(String name, String description, EventRule r1, EventRule r2, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        rule1 = r1;
        rule2 = r2;
    }


    /*redundant code*/
    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        /*
        Event res1 = rule1.testEvent(ie, true);
        Event res2 = rule2.testEvent(ie, true);

        if(res1 != null && (rule2.testEvent() != null) ){
            if(canFire() || override){
                return new Event(name, description, hideFromFeed);
            }
        }
        if ((res2 != null) && (rule1.testEvent() != null)){
            if(canFire() || override){
                return new Event(name, description, hideFromFeed);
            }
        }

        if (res1 != null && res2 != null){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }*/

        return null;
    }

    public Event testEvent(Event event) throws PhidgetException {
        if(rule1.isCorrespondingTo(event)){
            if(rule2.testEvent()!=null){
                return new Event(name, description, hideFromFeed);
            }
        }

        if(rule2.isCorrespondingTo(event)){
            if(rule1.testEvent() != null){
                return new Event(name, description, hideFromFeed);
            }
        }
        return null;
    }

    /*redundant code*/
    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        /*
        Event res1 = rule1.testEvent(se, true);
        Event res2 = rule2.testEvent(se, true);

        if((res1 != null) && (rule2.testEvent() != null) ){
            if(override || canFire() ){
                return new Event(name, description, hideFromFeed);
            }
        }
        if ((res2 != null) && (rule1.testEvent() != null)){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }

        if (res1 != null && res2 != null){
            if(override || canFire()){
                return new Event(name, description, hideFromFeed);
            }
        }
        */

        return null;
    }

    @Override
    public Event testEvent() throws PhidgetException {
        if((rule1.testEvent() != null) && (rule2.testEvent() != null)){
            return new Event(name, description, hideFromFeed);
        }
        return null;
    }

    @Override
    public type getType() {
        return type.AND;
    }
}
