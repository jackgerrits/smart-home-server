package com.jackgerrits;

import com.jackgerrits.handlers.PushHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by Jack on 21/03/2015.
 */
public class Server {
    int port;
    SensorController sensorController;
    HttpServer server = null;
    PushHandler ps;

    public Server(SensorController sensorController){
        this.port = utils.getServerPort();
        this.sensorController = sensorController;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps = new PushHandler(sensorController);
        server.createContext("/push", ps);
        server.createContext("/data/sensors", new SensorHandler(sensorController));
        server.createContext("/", new StaticHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }

    public void stop(){
        System.out.println("Server stopping...");
        server.stop(0);
        ps.stop();
    }


}
