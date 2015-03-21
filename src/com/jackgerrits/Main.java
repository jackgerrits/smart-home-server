package com.jackgerrits;

public class Main {

    public static void main(String[] args) {
        SensorController sc = new SensorController(utils.getSensors());
        Server server = new Server(sc);
    }
}
