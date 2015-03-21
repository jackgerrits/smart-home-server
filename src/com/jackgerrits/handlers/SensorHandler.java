package com.jackgerrits.handlers;

import com.jackgerrits.Sensor;
import com.jackgerrits.SensorController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jack on 21/03/2015.
 */
public class SensorHandler implements HttpHandler {
    SensorController sensorController;

    public SensorHandler(SensorController sc){
        this.sensorController = sc;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        URI uri = t.getRequestURI();
        String path = uri.getPath();
        String[] connectedSensors = sensorController.getConnectedSensors();

        System.out.println(uri.getPath());



        if (path.equals("/data/sensors")){
            JSONObject obj = new JSONObject();
            JSONArray sensors =  new JSONArray();

            for(String str : connectedSensors){
                sensors.add(str);
            }

            obj.put("sensors",sensors);
            t.sendResponseHeaders(200, obj.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(obj.toString().getBytes());
            os.close();
        } else if ((path.length() > 13) && (Arrays.asList(connectedSensors).contains(path.substring(14)))){
            String sensorName = path.substring(14);
            System.out.println(path.substring(14));
            JSONObject obj = new JSONObject();
            obj.put("value: ", sensorController.getVal(sensorName));
            obj.put("name: ", sensorName);
            t.sendResponseHeaders(200, obj.toString().length());
            OutputStream os = t.getResponseBody();
            os.write(obj.toString().getBytes());
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
