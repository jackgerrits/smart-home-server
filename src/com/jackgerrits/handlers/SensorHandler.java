package com.jackgerrits.handlers;

import com.jackgerrits.SensorController;
import com.jackgerrits.Server;
import com.phidgets.PhidgetException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
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
        Server server = Server.get();

        if(t.getRequestMethod().equals("OPTIONS")){
            server.handleOptionsRequest(t);
            return;
        }

        if( server.authRequest(t) && t.getRequestMethod().equals("POST")){
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
            } else if ((path.length() > 13) && (connectedSensors).contains(path.substring(14))){
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
                server.serve404(t);
            }
        }
    }
}
