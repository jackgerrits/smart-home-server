package com.jackgerrits.events;

import com.jackgerrits.Phidget;
import com.jackgerrits.Sensor;
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

    boolean isOccupied;
    HashMap<String, String> paramList;
    boolean isHidden;

    public EntityDetectionEventRule(HashMap<String,String> paramList,  boolean hideFromFeed, int timeout){
        super(paramList.get("name"), hideFromFeed, timeout);
        this.paramList = paramList;
        isHidden = hideFromFeed;
        MotionObserver mo = new MotionObserver();
        mo.setIsOccupied();
    }

    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL, ie.getSource());
        // if door sensor isn't defined hash table returns null, and causes if to be false
        if(eventSensor.getName().equals(paramList.get("door-sensor"))){
            if(canFire()){
                System.out.println("door activity detected, listening for significant motion now");
                MotionObserver mo = new MotionObserver();
                mo.startEventListenTimer();
            }
        }
        return null;
    }



    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());
        // if ir sensor isn't defined hash table returns null, and causes if to be false
//        System.out.println("IR event is tested with " + eventSensor.getName());
//        System.out.println(se.getSource().toString());
//        System.out.println(sensorController.getPhidget(paramList.get("ir-sensor")).getIK().toString());
//        System.out.println(sensorController.getPhidget(paramList.get("ir-sensor")).getIK()==se.getSource());
        if(eventSensor.getName().equals(paramList.get("ir-sensor"))){
            if(canFire()){
                System.out.println("door activity detected, listening for significant motion now");
                MotionObserver mo = new MotionObserver();
                mo.startEventListenTimer();
            }
        }
        return null;
    }


    @Override
    public Event testEvent() throws PhidgetException {
        if(isOccupied){
            return new Event(paramList.get("name-occupied"), paramList.get("description-occupied"), isHidden);
        } else {
            return new Event(paramList.get("name-absent"), paramList.get("description-absent"), isHidden);
        }
    }

    public String getParam(String key){
        return paramList.get(key);
    }

    @Override
    public boolean isCorrespondingTo(String eventName){
        return eventName.equals(name) ||
                eventName.equals(paramList.get("name-enter")) ||
                eventName.equals(paramList.get("name-leave")) ||
                eventName.equals(paramList.get("name-occupied")) ||
                eventName.equals(paramList.get("name-absent"));
    }

    boolean isOccupied(){
        return isOccupied;
    }



    /*
    Removed Door listener to stop the event because this was just an issue with the physical sensor setup.
    IR sensor should be positioned such that the door closing does not trigger it
    Door can actually be used as a trigger for this event
     */
    public class MotionObserver {
        Phidget motionPhidget;
        SensorChangeListener motionChangeListener;

        final int fluctuationThreshold = 2000;

        int maxValue = 500;
        int minValue = 500;
        int totalFluctuation = 0;
        int lastValue = 500;

        public MotionObserver(){
            motionPhidget = sensorController.getPhidget(paramList.get("motion-sensor"));
            if(motionPhidget==null){
                System.out.println("ERROR: motion sensor phidget could not be retrieved");
                System.exit(1);
            }

            motionChangeListener = new SensorChangeListener() {
                @Override
                public void sensorChanged(SensorChangeEvent sensorChangeEvent) {
                    Sensor eventSensor = sensorController.getSensor(sensorChangeEvent.getIndex(), Sensor.sensorType.ANALOG, sensorChangeEvent.getSource());
                    if(eventSensor.getName().equals(paramList.get("motion-sensor"))){
                        totalFluctuation += Math.abs(lastValue - sensorChangeEvent.getValue());
                        lastValue = sensorChangeEvent.getValue();
                        maxValue = lastValue > maxValue ? lastValue : maxValue;
                        minValue = lastValue < minValue ? lastValue : minValue;
                    }
                }
            };
        }

        public void setIsOccupied(){
            motionPhidget.attachListener(motionChangeListener);
            resetValues();

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("timer finished: max: " + maxValue + " min: " + minValue + " fluc:  " + totalFluctuation);
                    if(totalFluctuation>fluctuationThreshold ){
                        System.out.println("Setting initial to occupied");
                        isOccupied = true;
                    } else if(isOccupied){
                        System.out.println("Setting initial to empty");
                        isOccupied = false;
                    }
                    motionPhidget.removeListener(motionChangeListener);
                }
            }, 5000);
        }



        public void startEventListenTimer(){
            motionPhidget.attachListener(motionChangeListener);
            resetValues();

            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("timer finished: max: " + maxValue + " min: " + minValue + " fluc:  " + totalFluctuation);
                    if(totalFluctuation>fluctuationThreshold && !isOccupied ){
                        isOccupied = true;
                        sensorController.addEvent(new Event(paramList.get("name-enter"), paramList.get("description-enter"), isHidden));
                    } else if(totalFluctuation<fluctuationThreshold && isOccupied){
                        isOccupied = false;
                        sensorController.addEvent(new Event(paramList.get("name-leave"), paramList.get("description-leave"), isHidden));
                    }
                    motionPhidget.removeListener(motionChangeListener);
                }
            }, 5000);
        }

        private void resetValues(){
            maxValue = 500;
            minValue = 500;
            totalFluctuation = 0;
            lastValue = 500;
        }
    }
}
