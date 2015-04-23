package com.jackgerrits.handlers;

import com.jackgerrits.events.Event;
import com.jackgerrits.SensorController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Jack on 21/03/2015.
 */
public class FeedHandler implements HttpHandler {
    private SensorController sensorController;
    private boolean killed = false;

    public FeedHandler(){
        sensorController = SensorController.get();
    }

    public void handle(HttpExchange t) throws IOException {
        System.out.println("[Feed] Received feed request");

        //hold thread until there is an event ready
        while(!sensorController.areEvents()){
            if(killed){
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Event current = sensorController.getEvent();

        System.out.println("[Feed] Serving event: "+ current.getName());
        JSONObject obj = new JSONObject();
        obj.put("name", current.getName());
        obj.put("contents", current.getContents());
        obj.put("value", current.getValue());
        obj.put("rawTime", current.getTime());
        obj.put("time", current.getFormattedTime());
        t.sendResponseHeaders(200, obj.toString().length());
        OutputStream os = t.getResponseBody();
        os.write(obj.toString().getBytes());
        os.close();

    }

    public void stop(){
        killed = true;
    }
}
