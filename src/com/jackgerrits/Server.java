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

    public Server(SensorController sensorController){
        this.port = utils.getServerPort();
        this.sensorController = sensorController;

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        server.createContext("/data/sensors", new SensorHandler(sensorController));
        server.createContext("/push", new PushHandler());
        server.createContext("/", new StaticHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("about to start server");
        server.start();
    }


}
