package com.jackgerrits;

import com.jackgerrits.events.AndEventRule;
import com.jackgerrits.events.Event;
import com.jackgerrits.events.EventReader;
import com.jackgerrits.events.EventReader.RulesContainer;
import com.jackgerrits.events.EventRule;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.ArrayList;

/**
 * Created by jackgerrits on 23/03/15.
 */
public class EventTester {
    private ArrayList<EventRule> rules;
    private ArrayList<AndEventRule> andRules;
    Options ops;

    public EventTester(){
        ops = Options.get();
    }

    public void loadEvents(String filename){
        EventReader reader = new EventReader(filename);
        RulesContainer rc = reader.getContainer();

        rules = rc.getEventRules();
        andRules = rc.getAndEventRules();
    }

    public ArrayList<Event> evalEvent(Event event) {
        ArrayList<Event> outcomes = new ArrayList<>();
        for (AndEventRule rule : andRules){
            Event result = null;
            try {
                result = rule.test(event);
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
            if(result!=null){
                outcomes.add(result);
            }
        }
        return outcomes;
    }

    public ArrayList<Event> evalEvent(SensorChangeEvent se) {
        ArrayList<Event> outcomes = new ArrayList<>();
        if (rules != null){
            for(EventRule rule : rules) {

                Event result = null;
                try {
                    result = rule.test(se, false);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }

                if(result != null){
                    //System.out.println("Adding Event " + result.getName());
                    outcomes.add(result);
                }
            }
        }
        return outcomes;
    }
    public ArrayList<Event> evalEvent(InputChangeEvent ie) {
        ArrayList<Event> outcomes = new ArrayList<>();
        if (rules != null){
            for(EventRule rule : rules) {
                Event result = null;
                try {
                    result = rule.test(ie, false);
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }

                // System.out.println("[SENSOR ERROR] No such sensor at: [DIGITAL port: " + ie.getIndex() + ", value: " + ie.getState());
                if(result != null){
                    //System.out.println("Adding Event " + result.getName());
                    outcomes.add(result);
                }
            }
        }
        return outcomes;
    }
}
