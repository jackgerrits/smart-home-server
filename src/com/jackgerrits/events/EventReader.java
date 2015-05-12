package com.jackgerrits.events;

import com.jackgerrits.Options;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jack on 28/03/2015.
 */
public class EventReader {
    String filename;
    Options ops;
    RulesContainer container;

    public EventReader(String filename){
        this.filename = filename;
        ops = Options.get();
        container = new RulesContainer();
    }

    void processCurrent(JSONObject current){
        if(current.containsKey("type")){
            String type = (String) current.get("type");
            String name, description, sensor;
            Integer val;
            boolean hidden;
            int timeout;

            if(current.containsKey("hide-from-feed")){
                hidden = (boolean) current.get("hide-from-feed");
            } else {
                hidden = false;
            }

            if(current.containsKey("timeout")){
                timeout = ((Long)current.get("timeout")).intValue();
            } else {
                timeout = ops.getDefaultTimeout();
            }

            name = (String) current.get("name");
            if(container.getEventRule(name)!=null){
                System.out.println("JSON ERROR: Repeated event name: " + name);
                System.exit(1);
            }

            switch(type){
                case "change":
                    description = (String) current.get("description");
                    sensor = (String) current.get("sensor");

                    if(name == null || description == null || sensor == null){
                        System.out.println("JSON ERROR: Missing field. Required fields for change: [type, name, description, sensor]");
                        System.exit(1);
                    } else {
                        container.add(new ChangeEventRule(name, description, sensor, hidden, timeout));
                    }
                    break;
                case "equal":
                    description = (String) current.get("description");
                    sensor = (String) current.get("sensor");
                    val = ((Long) current.get("value")).intValue();


                    if(name == null || description == null || sensor == null || val == -1){
                        System.out.println("JSON ERROR: Missing field. Required fields for equal: [type, name, description, sensor, value]");
                        System.exit(1);
                    } else {
                        container.add(new EqualEventRule(name, description, sensor, val, hidden, timeout));
                    }

                    break;
                case "threshold":
                    String name_lt = (String) current.get("name_lt");
                    String name_gt = (String) current.get("name_gt");
                    String description_lt = (String) current.get("description_lt");
                    String description_gt = (String) current.get("description_gt");
                    sensor = (String) current.get("sensor");
                    val = ((Long) current.get("value")).intValue();

                    if(name == null || name_lt == null || name_gt == null || description_lt == null || description_gt == null ||  val == -1){
                        System.out.println("JSON ERROR: Missing field. Required fields for threshold: [type, name, name_lt, name_gt, description_lt, description_gt, sensor, value]");
                        System.exit(1);
                    } else {
                        container.add((new ThresholdEventRule(name, name_lt, name_gt, description_lt, description_gt, sensor, val, hidden, timeout)));
                    }
                    break;
                case "and":
                    description = (String) current.get("description");
                    String event1 = (String) current.get("event1");
                    String event2 = (String) current.get("event2");

                    if(name == null || description == null || event1 == null || event2 == null){
                        System.out.println("JSON ERROR: Missing field. Required fields for and: [type, name, description, event1, event2]");
                        System.exit(1);
                    } else {
                        container.add(new AndEventRule(name, description, event1, event2, hidden, timeout));
                    }
                    break;
                case "entityDetection":
                    if(container.getEntityDetectionEventRule() != null){
                        System.out.println("JSON ERROR: Cannot have more than one entityDetection eventRule");
                        System.exit(1);
                    }

                    HashMap<String,String> paramsList = new HashMap<>();

                    paramsList.put("name", (String)current.get("name"));
                    paramsList.put("name-enter", (String)current.get("name-enter"));
                    paramsList.put("name-leave", (String)current.get("name-leave"));
                    paramsList.put("name-occupied", (String)current.get("name-occupied"));
                    paramsList.put("name-absent", (String)current.get("name-absent"));
                    paramsList.put("description-leave", (String)current.get("description-leave"));
                    paramsList.put("description-enter", (String)current.get("description-enter"));
                    paramsList.put("description-occupied", (String)current.get("description-occupied"));
                    paramsList.put("description-absent", (String)current.get("description-absent"));
                    paramsList.put("motion-sensor", (String)current.get("motion-sensor"));

                    if(paramsList.values().contains(null)){
                        System.out.println("JSON ERROR: Missing field in entityDetection");
                        System.exit(1);
                    }

                    paramsList.put("ir-sensor", (String)current.get("ir-sensor"));
                    paramsList.put("door-sensor", (String)current.get("door-sensor"));

                    if(paramsList.get("ir-sensor") == null && paramsList.get("door-sensor") == null){
                        System.out.println("JSON ERROR: Must have at least one of ir-sensor or door-sensor defined for entityDetection eventRule");
                        System.exit(1);
                    }

                    container.add(new EntityDetectionEventRule(paramsList, hidden, timeout));
                    break;
                default:
                    System.out.println("JSON ERROR: Unknown type. Known types are: [change, equal, threshold, and, entityDetection]");
                    System.exit(1);
            }
        } else {
            System.out.println("JSON ERROR: Every event must have a type defined. Types are: [change, equal, threshold, and, entityDetection]");
            System.exit(1);
        }
    }


    public RulesContainer getContainer(){
        JSONParser parser = new JSONParser();
        FileReader fr = null;

        try {
            fr = new FileReader(filename);
            Object obj = parser.parse(fr);
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray events = (JSONArray) jsonObject.get("events");
            for(Object event: events){
                JSONObject current = (JSONObject) event;
                processCurrent(current);
            }
        } catch (FileNotFoundException e) {
            System.out.print("ERROR: "+ filename + " not found!");
            System.exit(1);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        if (fr != null) {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return container;
    }

    public class RulesContainer {
        ArrayList<AndEventRule> andEventRules;
        ArrayList<EventRule> eventRules;
        EntityDetectionEventRule et = null;


        public RulesContainer(){
            andEventRules = new ArrayList<>();
            eventRules = new ArrayList<>();
        }

        public EntityDetectionEventRule getEntityDetectionEventRule(){
            return et;
        }


        public void add(EventRule rule){
            if(rule instanceof AndEventRule){
                andEventRules.add((AndEventRule)rule);
            } else if(rule instanceof EntityDetectionEventRule) {
                et = (EntityDetectionEventRule)rule;
                eventRules.add(0,rule);
            } else {
                eventRules.add(rule);
            }
        }

        public ArrayList<AndEventRule> getAndEventRules(){
            return andEventRules;
        }

        public ArrayList<EventRule> getEventRules(){
            return eventRules;
        }

        public EventRule getEventRule(String name){
            for(EventRule rule : eventRules){
                if(rule.isCorrespondingTo(name)){
                    return rule;
                }
            }

            for(EventRule rule : andEventRules){
                if(rule.isCorrespondingTo(name)){
                    return rule;
                }
            }

            if(et!=null){
                if(et.isCorrespondingTo(name)){
                    return et;
                }
            }
            return null;
        }
    }
}
