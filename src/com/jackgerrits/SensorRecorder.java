package com.jackgerrits;

import com.jackgerrits.events.Event;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Class used to write sensor values to csv file for graphing.
 * @author jackgerrits
 */
public class SensorRecorder {
    PrintWriter file = null;
    String sensorName;
    Sensor.sensorType type;
    Phidget phidget;
    Object listener;
    SensorController sc;
    long startTime;

    /**
     * Constructs SensorRecorder for the given sensorName attaches listener for changes, writes changes to csv file named [sensorName]Vals.csv
     * @param sensorName name of sensor to record values for
     */
    public SensorRecorder(String sensorName){

        this.sensorName = sensorName;
        try {
            file = new PrintWriter(sensorName+"Vals.csv", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        file.println("time,value");
        sc =  SensorController.get();

        Sensor sensor = sc.getSensor(sensorName);
        //checks if the sensor doesnt exist
        if(sensor==null){
            System.out.println("ERROR sensor name ("+sensorName+") not found in SensorRecorder");
            System.exit(1);
        }
        type = sensor.getType();

        phidget =  sc.getPhidget(sensorName);
        listener = null;
        startTime = System.currentTimeMillis();

        //attaches corresponding listener depending on sensor type
        if(type==Sensor.sensorType.DIGITAL){
            listener = new InputChangeListener() {
                @Override
                public void inputChanged(InputChangeEvent ie) {
                    handleWrite(sc.getSensor(ie.getIndex(), Sensor.sensorType.DIGITAL, ie.getSource()));
                }
            };
            phidget.attachListener((InputChangeListener)listener);
        } else {
            listener = new SensorChangeListener() {
                @Override
                public void sensorChanged(SensorChangeEvent se) {
                    handleWrite(sc.getSensor(se.getIndex(), Sensor.sensorType.ANALOG, se.getSource()));
                }
            };
            phidget.attachListener((SensorChangeListener)listener);
        }
    }

    private void handleWrite(Sensor eventSensor) {
        if (eventSensor != null && eventSensor.getName().equals(sensorName)) {
            try {
                file.println(getSeconds() + "," + sc.getVal(eventSensor));
                file.flush();
            } catch (PhidgetException e) {
                e.printStackTrace();
            }
        }
    }

    private double getSeconds(){
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

    /**
     * Stops recorder by removing change listener and closing the file.
     */
    public void stop(){
        if(type==Sensor.sensorType.DIGITAL){
            phidget.removeListener((InputChangeListener)listener);
        } else {
            phidget.removeListener((SensorChangeListener)listener);
        }
        file.close();
    }
}