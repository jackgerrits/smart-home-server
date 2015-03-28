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

    private String name;
    private String description;
    private EventRule rule1;
    private EventRule rule2;
    private SensorController sensorController;


    /*
     * AND event works for CHANGE and EQUAL events (Currently threshold events arent supported due to complexity)
     * Two things cannot change at the same moment so therefoer
     */

    public AndEventRule(String name, String description, EventRule r1, EventRule r2, SensorController sc, Options ops){
        super(ops);
        this.name = name;
        this.description = description;
        rule1 = r1;
        rule2 = r2;
        sensorController = sc;
    }

    @Override
    public Event test(InputChangeEvent ie, boolean override) throws PhidgetException {
        Event res1 = rule1.test(ie, true);
        Event res2 = rule2.test(ie, true);

        if(res1 != null && (rule2.test() != null) ){
            if(canFire()){
                return new Event(name, description);
            }
        } else if ((res2 != null) && (rule1.test() != null)){
            if(canFire()){
                return new Event(name, description);
            }
        }
        return null;
    }

    @Override
    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        Event res1 = rule1.test(se, true);
        Event res2 = rule2.test(se, true);

        if((res1 != null) && (rule2.test() != null) ){
            if(canFire() || override){
                return new Event(name, description);
            }
        } else if ((res2 != null) && (rule1.test() != null)){
            if(canFire() || override){
                return new Event(name, description);
            }
        }
        return null;
    }

    @Override
    public Event test() throws PhidgetException {
        if((rule1.test() != null) && (rule2.test() != null)){
            if(canFire()){
                return new Event(name, description);
            }
        }
        return null;
    }

    @Override
    public type getType() {
        return type.AND;
    }
}
