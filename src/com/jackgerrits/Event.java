package com.jackgerrits;

/**
 * Created by Jack on 21/03/2015.
 */
public class Event {
    String sensor;
    String contents;
    String time;

    public Event(String sensor, String contents, String time){
        this.sensor = sensor;
        this.contents = contents;
        this.time = time;
    }
}
