package com.jackgerrits;

import com.jackgerrits.handlers.PushHandler;
import com.jackgerrits.handlers.SensorHandler;
import com.jackgerrits.handlers.StaticHandler;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by Jack on 21/03/2015.
 */
public class Server {
    int port;
    SensorController sensorController;
    HttpServer server = null;
    PushHandler ps;

    //runs webserver and application server
    public Server(SensorController sensorController){
        this.port = utils.getServerPort();
        this.sensorController = sensorController;

        BasicAuthenticator bAuth = new BasicAuthenticator("get") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("admin") && pwd.equals("password");
            }
        };

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps = new PushHandler(sensorController);
        server.createContext("/push", ps).setAuthenticator(bAuth);
        server.createContext("/data/sensors", new SensorHandler(sensorController)).setAuthenticator(bAuth);
        server.createContext("/", new StaticHandler()).setAuthenticator(bAuth);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }

    //just runs web server
    public Server(){
        this.port = utils.getServerPort();
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpContext hc = server.createContext("/", new StaticHandler());
        hc.setAuthenticator(new BasicAuthenticator("get") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("admin") && pwd.equals("password");
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Starting server on port " + port + "...");
        server.start();
        System.out.println("Server started successfully!");
    }

    public void stop(){
        System.out.println("Server stopping...");
        server.stop(0);
        if(ps != null){
            ps.stop();
        }
    }
}
