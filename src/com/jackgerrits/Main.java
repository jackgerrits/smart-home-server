package com.jackgerrits;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Server server = new Server();

        System.out.println("\nPress enter to end...\n");

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stop();
    }
}
