package com.jackgerrits.events;

import com.jackgerrits.Phidget;
import com.jackgerrits.Sensor;
import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jack on 23/04/2015.
 */
public class EntityDetectionEventRule extends EventRule{


    boolean isOccupied = true;
    HashMap<String, String> paramList;
    boolean isHidden;

    public EntityDetectionEventRule(HashMap<String,String> paramList,  boolean hideFromFeed, int timeout){
        super(paramList.get("name"), hideFromFeed, timeout);
        this.paramList = paramList;
        isHidden = hideFromFeed;

        /*TODO
        start a timer here which fires every 5 mins or so and checks if there is movement in the room
        this might be a bad idea though for instance when people are sleeping
         */
    }

    public String getParam(String key){
        return paramList.get(key);
    }

    @Override
    public boolean isCorrespondingTo(String eventName){
        if(eventName.equals(name) ||
                eventName.equals(paramList.get("name-enter")) ||
                eventName.equals(paramList.get("name-leave")) ||
                eventName.equals(paramList.get("name-occupied")) ||
                eventName.equals(paramList.get("name-absent"))){
            return true;
        }
        return false;
    }



    @Override
    public Event test(InputChangeEvent ie, boolean override) throws PhidgetException {
        /* Currently entity detection only relies on ir sensor to begin the process */
        return null;
    }

    @Override
    public Event test(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG);
        if(eventSensor.getName().equals(paramList.get("ir-sensor"))){
            if(canFire()){
                System.out.println("door activity detected, listening for significant motion now");
                MotionObserver mo = new MotionObserver();
                mo.start();
            }
        }
        return null;
    }

    boolean isOccupied(){
        return isOccupied;
    }

    @Override
    public Event test() throws PhidgetException {
        if(isOccupied){
            return new Event(paramList.get("name-occupied"), paramList.get("description-occupied"), isHidden);
        } else {
            return new Event(paramList.get("name-absent"), paramList.get("description-absent"), isHidden);
        }
    }

    @Override
    public type getType() {
        return type.ENTITY_DETECTION;
    }

    public class MotionObserver {
        Phidget motionPhidget;
        Phidget doorPhidget;
        SensorChangeListener motionChangeListener;
        InputChangeListener doorChangeListener;

        int maxValue = 500;
        int minValue = 500;
        int totalFluctuation = 0;
        int lastValue = 500;
        boolean doorDetected = false;


        public MotionObserver(){
            motionPhidget = sensorController.getPhidget(paramList.get("motion-sensor"));
            doorPhidget = sensorController.getPhidget(paramList.get("door-sensor"));
            if(motionPhidget==null){
                System.out.println("ERROR: motion sensor phidget could not be retrieved");
                System.exit(1);
            }

            motionChangeListener = new SensorChangeListener() {
                @Override
                public void sensorChanged(SensorChangeEvent sensorChangeEvent) {
                    Sensor eventSensor = sensorController.getSensor(sensorChangeEvent.getIndex(), Sensor.sensorType.ANALOG);
                    if(eventSensor.getName().equals(paramList.get("motion-sensor"))){
                        totalFluctuation += Math.abs(lastValue - sensorChangeEvent.getValue());
                        lastValue = sensorChangeEvent.getValue();
                        maxValue = lastValue > maxValue ? lastValue : maxValue;
                        minValue = lastValue < minValue ? lastValue : minValue;
                    }
                }
            };

            doorChangeListener = new InputChangeListener() {
                @Override
                public void inputChanged(InputChangeEvent inputChangeEvent) {
                    Sensor eventSensor = sensorController.getSensor(inputChangeEvent.getIndex(), Sensor.sensorType.DIGITAL);
                    if(eventSensor.getName().equals(paramList.get("door-sensor"))){
                        System.out.println("The door has been detected");
                       doorDetected = true;
                    }
                }
            };
        }

        public void start(){
            motionPhidget.attachListener(motionChangeListener);
            doorPhidget.attachListener(doorChangeListener);

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("timer finished: max: " + maxValue + " min: " + minValue + " fluc:  " + totalFluctuation);
                    if(totalFluctuation>2000 && !isOccupied  && !doorDetected){
                        isOccupied = true;
                        sensorController.addEvent(new Event(paramList.get("name-enter"), paramList.get("description-enter"), isHidden));
                    } else if(isOccupied && !doorDetected){
                        isOccupied = false;
                        sensorController.addEvent(new Event(paramList.get("name-leave"), paramList.get("description-leave"), isHidden));
                    }
                    motionPhidget.removeListener(motionChangeListener);
                    doorPhidget.removeListener(doorChangeListener);
                }
            }, 5000);
        }
    }
}
