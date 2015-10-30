package com.jackgerrits;

import java.io.IOException;

/**
 * Main driver class
 * @author jackgerrits
 */
public class Main {

    public static void main(String[] args) {
        //creating the Sever object and then start it
        Server server = new Server();
        server.start();

        System.out.println("\nPress enter to end...\n");

        // Read in user input
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.stop();
    }
}
