package com.jackgerrits.events;

import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

/**
 * EventRule for defining an AND relationship with other events
 * @author jackgerrits
 */
public class AndEventRule extends EventRule {
    private String description;
    String event1;
    String event2;

    /**
     * Constructs AndEventRule object for the given parameters
     * @param name Name of the event to generate and this event rule
     * @param description Description of what the EventRule represents
     * @param event1 name of the first rule in the and relationship
     * @param event2 name of the second rule in the and relationship
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
    public AndEventRule(String name, String description, String event1, String event2, boolean hideFromFeed, int timeout){
        super(name, hideFromFeed, timeout);
        this.description = description;
        this.event1 = event1;
        this.event2 = event2;
    }


    /**
     * This is an unused method as AndEventRule is not used to test InputChangeEvents
     */
    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        return null;
    }

    /**
     * This is an unused method as AndEventRule is not used to test SensorChangeEvents
     */
    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        return null;
    }

    /**
     * Accepts an event and tests whether it is part of the AND relationship, and then tests for the second AND event
     * @param event initial event to test
     * @return Event if the AND relationship is satisfied
     */
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

    /**
     * Tests whether an event name corresponds to this event rule
     * @param eventName name of event to test
     * @return true if it corresponding
     */
    @Override
    public boolean isCorrespondingTo(String eventName) {
        return eventName.equals(name);
    }

    /**
     * Tests both events in the AndEventRule to see if it is true
     * @return Event if both events are satisfied
     */
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

}
