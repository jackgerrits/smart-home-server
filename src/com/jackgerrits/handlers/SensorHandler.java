package com.jackgerrits.handlers;

import com.jackgerrits.SensorController;
import com.phidgets.PhidgetException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Jack on 21/03/2015.
 */
public class SensorHandler implements HttpHandler {
    SensorController sensorController;
    ArrayList<String> connectedSensors;

    public SensorHandler(){
        sensorController = SensorController.get();
        connectedSensors = sensorController.getConnectedSensors();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        URI uri = t.getRequestURI();
        String path = uri.getPath();

        if (path.equals("/data/sensors")){
            JSONObject obj = new JSONObject();
            JSONArray sensors =  new JSONArray();

            Collections.addAll(sensors, connectedSensors);

            obj.put("sensors",sensors);
            System.out.println("[Sensors] Serving: /data/sensors");
            t.sendResponseHeaders(200, obj.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(obj.toString().getBytes());
            os.close();
        } else if ((path.length() > 13) && (Arrays.asList(connectedSensors).contains(path.substring(14)))){
            String sensorName = path.substring(14);
            System.out.println("[Sensors] Serving: /data/sensors/"+ sensorName);
            JSONObject obj = new JSONObject();
            int value = -1;
            try{
                value = sensorController.getVal(sensorName);
            } catch (PhidgetException e){
                System.out.println(e.getErrorNumber());
                System.out.println(e.getDescription());
                /* TODO determine cause of Phidget exception
                    -determine if no sensor being plugged into analog port throws an error and what error code
                        CANNOT TELL IF THERE IS NO SENSOR
                        JUST RETURNS ZERO
                    -handle error if connection to Phidget is lost (Network lost)

                 */
            }

            OutputStream os = t.getResponseBody();

            if(value != -1){
                obj.put("value", value);
                obj.put("name", sensorName);
                t.sendResponseHeaders(200, obj.toString().length());
                os.write(obj.toString().getBytes());
            } else {
                String response = "<h1>500 - Server Error</h1> <br /> " +
                        "Lost connection to Phidget.";
                t.sendResponseHeaders(500, response.length());
                os.write(response.getBytes());
            }

            os.close();
        }
        else {
            String response = "<h1>404 - Not Found</h1>\n";
            t.sendResponseHeaders(404, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
