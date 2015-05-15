package com.jackgerrits.handlers;

import com.jackgerrits.SensorController;
import com.jackgerrits.Server;
import com.phidgets.PhidgetException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Handler for sensor route, serves either all connected sensors or specific sensor values depending on path
 * @author Jack Gerrits
 */
public class SensorHandler implements HttpHandler {
    SensorController sensorController;
    ArrayList<String> connectedSensors;

    public SensorHandler(){
        sensorController = SensorController.get();
        connectedSensors = sensorController.getConnectedSensors();
    }

    /**
     * verifies that the path provided refers to a sensor that exists
     * @param path path to check
     * @return true if the path is valid
     */
    private boolean isValidSensorPath(String path){
        String sensorName = path.substring(14);
        return (path.length() > 13) && connectedSensors.contains(sensorName);
    }

    /**
     * Handler for sensor route, serves either all connected sensors or specific sensor values depending on path
     * @param t HttpExchange to respond to
     */
    @Override
    public void handle(HttpExchange t) throws IOException {
        URI uri = t.getRequestURI();
        String path = uri.getPath();
        Server server = Server.get();

        //Responds to cross origin preflight request
        if(t.getRequestMethod().equals("OPTIONS")){
            server.handleOptionsRequest(t);
            return;
        }

        //if it didn't authenticate or the request then do not go any further
        // auth request will respond to the client
        if( !server.authRequest(t)){
            return;
        }

        if (path.equals("/data/sensors")){
            //responds to "/data/sensors" with a JSON object of all connected sensors
            JSONObject obj = new JSONObject();
            JSONArray sensors =  new JSONArray();

            Collections.addAll(sensors, connectedSensors);
            obj.put("sensors",sensors);

            System.out.println("[Sensors] Serving: /data/sensors");
            t.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); //allows sensor data to be retrieved even if client is different to application server
            t.sendResponseHeaders(200, obj.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(obj.toString().getBytes());
            os.close();
        } else if (isValidSensorPath(path)){
            //extracts everything after "/data/sensors/" and checks if it is one of the connected sensors
            String sensorName = path.substring(14);
            System.out.println("[Sensors] Serving: /data/sensors/"+ sensorName);
            JSONObject obj = new JSONObject();
            int value = -1;
            try{
                value = sensorController.getVal(sensorName);
            } catch (PhidgetException e){
                System.out.println(e.getErrorNumber());
                System.out.println(e.getDescription());
            }

            OutputStream os = t.getResponseBody();

            //-1 means there was an error retrieving the value
            if(value != -1){
                obj.put("value", value);
                obj.put("name", sensorName);
                t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
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
            //if that sensor isnt found then it responds with 404
            server.serve404(t);
        }
    }
}