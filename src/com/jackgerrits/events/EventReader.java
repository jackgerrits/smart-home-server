package com.jackgerrits.events;

import com.jackgerrits.Options;
import com.jackgerrits.SensorController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jack on 28/03/2015.
 */
public class EventReader {
    String filename;
    SensorController sensorController;
    Options ops;
    ArrayList<EventRule> rules;

    public EventReader(String filename, SensorController sensorController, Options ops){
        this.filename = filename;
        this.sensorController = sensorController;
        this.ops = ops;
        rules = new ArrayList<>();
    }

    public ArrayList<EventRule> getEventRules(){
        JSONParser parser = new JSONParser();
        FileReader fr = null;

        try {
            fr = new FileReader(filename);
            Object obj = parser.parse(fr);
            JSONObject jsonObject =  (JSONObject) obj;
            JSONArray events = (JSONArray) jsonObject.get("events");
            for(Object event: events){
                JSONObject current = (JSONObject) event;
                if(current.containsKey("type")){
                    String type = (String) current.get("type");
                    String name, description, sensor = null;
                    Integer val = null;

                    name = (String) current.get("name");
                    if(getEventRule(name)!=null){
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
                                rules.add(new ChangeEventRule(name, description, sensor, sensorController, ops));
                            }
                            break;
                        case "equal":
                            description = (String) current.get("description");
                            sensor = (String) current.get("sensor");
                            val = new Long((Long) current.get("value")).intValue();


                            if(name == null || description == null || sensor == null || val == -1){
                                System.out.println("JSON ERROR: Missing field. Required fields for equal: [type, name, description, sensor, value]");
                                System.exit(1);
                            } else {
                                rules.add(new EqualEventRule(name, description, sensor, val, sensorController, ops));
                            }

                            break;
                        case "threshold":
                            String name_lt = (String) current.get("name_lt");
                            String name_gt = (String) current.get("name_gt");
                            String description_lt = (String) current.get("description_lt");
                            String description_gt = (String) current.get("description_gt");
                            sensor = (String) current.get("sensor");
                            val = new Long((Long) current.get("value")).intValue();

                            if(name == null || name_lt == null || name_gt == null || description_lt == null || description_gt == null ||  val == -1){
                                System.out.println("JSON ERROR: Missing field. Required fields for threshold: [type, name, name_lt, name_gt, description_lt, description_gt, sensor, value]");
                                System.exit(1);
                            } else {
                                rules.add(new ThresholdEventRule(name, name_lt, name_gt, description_lt, description_gt, sensor, val, sensorController, ops));
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
                                EventRule r1 = getEventRule(event1);
                                EventRule r2 = getEventRule(event2);

                                if( r1 == null || r2 == null){
                                    System.out.println("JSON ERROR: Supplied EventRules for AND are not defined.");
                                    System.exit(1);
                                }

                                rules.add(new AndEventRule(name, description, r1, r2, sensorController, ops));
                            }
                            break;

                        default:
                            System.out.println("JSON ERROR: Unknown type. Known types are: [change, equal, threshold, and]");
                            System.exit(1);
                    }
                } else {
                    System.out.println("JSON ERROR: Every event must have a type defined. Types are: [change, equal, threshold, and]");
                    System.exit(1);
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (fr != null) {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rules;
    }

    public EventRule getEventRule(String name){
        for(EventRule rule : rules){
            if(rule.getName().equals(name)){
                return rule;
            }
        }
        return null;
    }
}