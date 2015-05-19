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
 * Event rule for detecting whether a room is occupied or not, uses motion sensor and ir distance sensor and/or magnetic switch
 * The magnetic switch is intended for detecting whether a door is open or closed
 * The ir distance sensor is intended for detecting whether a doorway is walked through, place it facing across the doorway
 * The motion sensor is for detecting movement in the room, place it such that it is facing into the room.
 * @author jackgerrits
 */
public class EntityDetectionEventRule extends EventRule{

    boolean isOccupied;
    HashMap<String, String> paramList;
    boolean isHidden;
    int fluctuationThresh;

    /**
     * Constructs an EntityDetectionEventRule object
     * Paramlist contains
     * @param paramList HashMap of values required for the event rule, see example events.json for fields
     * @param hideFromFeed true to hide event from feed pushed to client
     * @param timeout timeout between event fires, -1 to use default from options
     */
    public EntityDetectionEventRule(HashMap<String,String> paramList, int fluctuationThresh, boolean hideFromFeed, int timeout){
        super(paramList.get("name"), hideFromFeed, timeout);
        this.paramList = paramList;
        isHidden = hideFromFeed;
        this.fluctuationThresh = fluctuationThresh;
        MotionObserver mo = new MotionObserver();
        mo.setIsOccupied();
    }

    /**
     * Tests an input change event to see if this event happened or not
     * @param ie InputChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if event occurred, or null if there was no event determined
     */
    @Override
    public Event testEvent(InputChangeEvent ie, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL, ie.getSource());
        // if door sensor isn't defined hash table returns null, and causes if to be false
        if(eventSensor.getName().equals(paramList.get("door-sensor"))){
           handleProximityDetection();
        }
        //this function always returns null as the MotionObserver created in handleProximityDetection add events to queue
        return null;
    }

    /**
     * Tests an sensor change event to see if this event happened or not
     * @param se SensorChangeEvent from Phidget to test
     * @param override overrides the firing timeout
     * @return Event if event occurred, or null if there was no event determined
     */
    @Override
    public Event testEvent(SensorChangeEvent se, boolean override) throws PhidgetException {
        Sensor eventSensor = sensorController.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource());
        if(eventSensor.getName().equals(paramList.get("ir-sensor"))){
            handleProximityDetection();
        }
        //this function always returns null as the MotionObserver created in handleProximityDetection add events to queue
        return null;
    }

    /**
     * Creates MotionObserver and starts its listening
     */
    private void handleProximityDetection(){
        if(canFire()){
            System.out.println("[Entity Detection] Boundary crossed, listening for significant motion now");
            MotionObserver mo = new MotionObserver();
            mo.startEventListenTimer();
        }
    }


    /**
     * Used to test whether the room is currently occupied or not
     * @return either absent or occupied Event object
     */
    @Override
    public Event testEvent() throws PhidgetException {
        if(isOccupied){
            return new Event(paramList.get("name-occupied"), paramList.get("description-occupied"), isHidden);
        } else {
            return new Event(paramList.get("name-absent"), paramList.get("description-absent"), isHidden);
        }
    }

    /**
     * Gets the corresponding value for given key
     * @param key key to find
     * @return value for key, or null if not found
     */
    public String getParam(String key){
        return paramList.get(key);
    }

    /**
     * Tests whether a particular string represents an Event created by this event rule
     * @param eventName name of event to test
     * @return true if the name is corresponding
     */
    @Override
    public boolean isCorrespondingTo(String eventName){
        return eventName.equals(name) ||
                eventName.equals(paramList.get("name-enter")) ||
                eventName.equals(paramList.get("name-leave")) ||
                eventName.equals(paramList.get("name-occupied")) ||
                eventName.equals(paramList.get("name-absent"));
    }

    /**
     * Tests whether the room is currently occupied, provides a public way to access this boolean value without being able to modify
     * @return true if the room is occupied
     */
    boolean isOccupied(){
        return isOccupied;
    }

    /**
     * Listens for motion in room, if the room is deemed from absent->occupied or occupied->absent, adds the respective Event into the queue
     */
    public class MotionObserver {
        Phidget motionPhidget;
        SensorChangeListener motionChangeListener;

        int maxValue = 500;
        int minValue = 500;
        int totalFluctuation = 0;
        int lastValue = 500;

        /**
         * contructs MotionObserver object and attaches a SensorChangeListener to the motion sensor, begins listening
         */
        public MotionObserver(){
            motionPhidget = sensorController.getPhidget(paramList.get("motion-sensor"));
            if(motionPhidget==null){
                System.out.println("ERROR: motion sensor phidget could not be retrieved");
                System.exit(1);
            }

            //creates the SensorChangeListener
            motionChangeListener = new SensorChangeListener() {
                @Override
                public void sensorChanged(SensorChangeEvent sensorChangeEvent) {
                    Sensor eventSensor = sensorController.getSensor(sensorChangeEvent.getIndex(), Sensor.sensorType.ANALOG, sensorChangeEvent.getSource());
                    if(eventSensor.getName().equals(paramList.get("motion-sensor"))){
                        //adds the difference between last value and now to the total fluctuation
                        totalFluctuation += Math.abs(lastValue - sensorChangeEvent.getValue());
                        lastValue = sensorChangeEvent.getValue();
                        maxValue = lastValue > maxValue ? lastValue : maxValue;
                        minValue = lastValue < minValue ? lastValue : minValue;
                    }
                }
            };
        }

        /**
         * initial setup for EntityDetectionRule, determines if room is occupied or not
         */
        public void setIsOccupied(){
            motionPhidget.attachListener(motionChangeListener);
            resetValues();

            //creates a timer which fires after 5 seconds
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("[Entity Detection] Timer finished: {max: " + maxValue + ", min: " + minValue + ", fluc:  " + totalFluctuation+"}");
                    if(totalFluctuation>fluctuationThresh) {
                        System.out.println("[Entity Detection] Setting initial to occupied");
                        isOccupied = true;
                    } else {
                        System.out.println("[Entity Detection] Setting initial to empty");
                        isOccupied = false;
                    }
                    motionPhidget.removeListener(motionChangeListener);
                }
            }, 5000);
        }

        /**
         * starts the 5 second period where it listens for motion, if an occupancy change occurs, adds an event to the queue
         */
        public void startEventListenTimer(){
            motionPhidget.attachListener(motionChangeListener);
            resetValues();

            //creates a timer which fires after 5 seconds
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("[Entity Detection] Timer finished: {max: " + maxValue + ", min: " + minValue + ", fluc:  " + totalFluctuation+"}");
                    if(totalFluctuation>fluctuationThresh && !isOccupied ){
                        isOccupied = true;
                        //adds the corresponding 
                        sensorController.addEvent(new Event(paramList.get("name-enter"), paramList.get("description-enter"), isHidden));
                    } else if(totalFluctuation<fluctuationThresh && isOccupied){
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
