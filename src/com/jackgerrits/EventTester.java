package com.jackgerrits;

import com.jackgerrits.events.*;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.SensorChangeEvent;

import java.util.ArrayList;

/**
 * Created by jackgerrits on 23/03/15.
 */
public class EventTester {
    private ArrayList<EventRule> rules;
    private SensorController sensorController;
    Options ops;

    public EventTester(SensorController sensorController, Options ops){
        this.sensorController = sensorController;
        this.ops = ops;
    }

    public void loadEvents(String filename){
        EventReader reader = new EventReader(filename, sensorController, ops);
        rules = reader.getEventRules();
//        rules = new ArrayList<>();
//        rules.add(new EqualEventRule("doorOpened", "Door opened.", "magSwitch", 0, sensorController,ops));
//        rules.add(new EqualEventRule("doorClosed", "Door closed.", "magSwitch", 1, sensorController, ops));
//        rules.add(new ChangeEventRule("touch", "Touch sensor touched.", "touch", sensorController, ops));
//        rules.add(new AndEventRule("alarm", "door open and sensor touched", rules.get(2), rules.get(0), sensorController, ops));
//        rules.add(new ThresholdEventRule("name", "lightOff", "lightOn", "Room is dark.", "Room is bright.", "light", 30, sensorController, ops));

    }

    public ArrayList<Event> evalEvent(SensorChangeEvent se)  throws PhidgetException {
        ArrayList<Event> outcomes = new ArrayList<>();
        if (rules != null){
            for(EventRule rule : rules) {
                Event result = rule.test(se, false);
                if(result != null){
                    outcomes.add(result);
                }
            }
        }
        return outcomes;
    }
    public ArrayList<Event> evalEvent(InputChangeEvent ie) throws PhidgetException {
        ArrayList<Event> outcomes = new ArrayList<>();
        if (rules != null){
            for(EventRule rule : rules) {
                Event result = rule.test(ie, false);
                if(result != null){
                    outcomes.add(result);
                }
            }
        }
        return outcomes;
    }
}
