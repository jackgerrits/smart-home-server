package com.jackgerrits.events;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * Created by Jack on 28/03/2015.
 */
public class AndEventRule extends EventRule {

    private String description;
    String event1;
    String event2;

    /*
     * AND event works for CHANGE, EQUAL, THRESHOLD
     * For testing whether event causes threshold to trip, standard threshold events can be added.
     * To testEvent whether state is in either of two threshold states, ThreshBundleEventRule must be passed containing the Threshold event and sub event name
     */

    public AndEventRule(String name, String description, String event1, String event2, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.event1 = event1;
        this.event2 = event2;
    }


    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        return null;
    }

    public Event testEvent(Event event) throws PhidgetException {
        if(event.getName().equals(event1)){
            Event testResult = sensorController.evalEvent(event2);
            if(testResult != null && testResult.getName().equals(event2)){
                return new Event(name, description, hideFromFeed);
            }
        } else if(event.getName().equals(event2)){
            Event testResult = sensorController.evalEvent(event1);
            if(testResult != null && testResult.getName().equals(event1)){
                return new Event(name, description, hideFromFeed);
            }
        }
        return null;
    }

    @Override
    public boolean isCorrespondingTo(String eventName) {
        return eventName.equals(name);
    }

    /*redundant code*/
    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        return null;
    }

    @Override
    public Event testEvent() throws PhidgetException {
        Event resEvent1 = sensorController.evalEvent(event1);
        Event resEvent2 = sensorController.evalEvent(event2);

        if(resEvent1 == null || resEvent2 == null){
            return null;
        }

        if(resEvent1.getName().equals(event1) && resEvent2.getName().equals(event2)){
            return new Event(name, description, hideFromFeed);
        }
        return null;
    }

    @Override
    public type getType() {
        return type.AND;
    }
}
