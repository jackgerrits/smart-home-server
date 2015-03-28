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
    ArrayList<EventRule> rules;
    SensorController sensorController;

    public EventTester(SensorController sensorController){
        this.sensorController = sensorController;
    }

    public void loadEvents(String filename){
        rules = new ArrayList<>();
//        rules.add(new EventRule("door", "Door open", "magSwitch", 0, sensorController));
        rules.add(new EqualEventRule("doorOpened", "Door opened.", "magSwitch", 0, sensorController));
        rules.add(new EqualEventRule("doorClosed", "Door closed.", "magSwitch", 1, sensorController));
        rules.add(new ChangeEventRule("touch", "Touch sensor touched.", "touch", sensorController));
        rules.add(new ThresholdEventRule("lightOff", "lightOn", "Room is dark.", "Room is bright.", "light", 30, sensorController));
//        rules.add(new EventRule("door", "Door closed", "magSwitch", 1, sensorController, EventRule.condition.EQUAL));
//        rules.add(new EventRule("touch", "Touch detected", "touch", 1, sensorController, EventRule.condition.CHANGE));
//        rules.add(new EventRule("light", "Room is bright", "light", 30, sensorController, EventRule.condition.GT));
//        rules.add(new EventRule("light", "Room is dark", "light", 30, sensorController, EventRule.condition.LT));

    }

    public Event evalEvent(SensorChangeEvent se)  throws PhidgetException {
        if (rules != null){
            for(EventRule rule : rules) {
                Event result = rule.test(se);
                if(result != null){
                    return result;
                }
            }
        }
        return null;
    }
    public Event evalEvent(InputChangeEvent ie) throws PhidgetException {
        if (rules != null){
            for(EventRule rule : rules){
                Event result = rule.test(ie);
                if(result != null){
                    return result;
                }
            }
        }
        return null;
    }
}
