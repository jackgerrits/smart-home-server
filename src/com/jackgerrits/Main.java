package com.jackgerrits;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SensorController sc = new SensorController(utils.getSensors());
        Server server = new Server(sc);

        System.out.println("\nPress enter to end...");

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
    }
}
