package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.jackgerrits.events.EventReader.RulesContainer;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.ArrayList;

/**
 * Tests the provided change events with the user defined events
 * @author jackgerrits
 */
public class EventTester {
    private ArrayList<EventRule> rules;
    private ArrayList<AndEventRule> andRules;
    Options ops;

    /**
     * contructs an event tester object
     */
    public EventTester(){
        ops = Options.get();
    }

    /**
     * loads the user defined events from file
     * must be called before checking for events, otherwise nothing will be checked
     * @param filename file to load events from
     */
    public void loadEvents(String filename){
        EventReader reader = new EventReader(filename);
        RulesContainer rc = reader.getContainer();

        rules = rc.getEventRules();
        andRules = rc.getAndEventRules();
    }

    /**
     * Evaluates an event by the name of it
     * @param eventName name of event to test
     * @return Event if an event was found, otherwise null
     */
    public Event evalEvent(String eventName){
        ArrayList<EventRule> allRules = new ArrayList<>(rules);
        allRules.addAll(andRules);

        //finds the corresponding event rule and tests it
        for(EventRule e : allRules){
            if(e.isCorrespondingTo(eventName)){
                try {
                    return e.testEvent();
                } catch (PhidgetException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    //when an event is passed in to be evaluated then it is being testEvent for AND events

    /**
     * Evaluates an inputted Event to see if it is part of an AND event
     * @param event Event to be tested
     * @return ArrayList of Events that were found, empty if no Events
     */
    public ArrayList<Event> evalEvent(Event event) {
        ArrayList<Event> outcomes = new ArrayList<>();
        for (AndEventRule rule : andRules){
            Event result = null;
            try {
                result = rule.testEvent(event);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
            if(result!=null){
                outcomes.add(result);
            }
        }
        return outcomes;
    }

    /**
     * Evaluates change event from Phidget change listener
     * @param changeEvent InputChangeEvent or SensorChangeEvent to be tested
     * @return ArrayList of Events that were found, otherwise null
     */
    public ArrayList<Event> evalEvent(Object changeEvent) {
        ArrayList<Event> outcomes = new ArrayList<>();
        if (rules != null){
            for(EventRule rule : rules) {

                Event result = null;
                try {
                    if(changeEvent instanceof SensorChangeEvent){
                        result = rule.testEvent((SensorChangeEvent) changeEvent, false);
                    } else if (changeEvent instanceof InputChangeEvent){
                        result = rule.testEvent((InputChangeEvent) changeEvent, false);
                    }
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }

                if(result != null){
                    outcomes.add(result);
                }
            }
        }
        return outcomes;
    }

}
