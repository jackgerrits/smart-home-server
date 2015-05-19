package com.jackgerrits.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Data structure for events
 * @author jackgerrits
 */
public class Event {
    private String name;
    private String contents;
    private long time;
    boolean hideFromFeed;
    int value;

    /**
     * Constructs an Event
     * @param name name of the event
     * @param contents description of the event
     * @param hideFromFeed whether to hide the event from the client feed
     */
    public Event(String name, String contents, boolean hideFromFeed){
        this.name = name;
        this.contents = contents;
        this.time = System.currentTimeMillis();
        this.hideFromFeed = hideFromFeed;
        value = 0;
    }

    /**
     * Constructs an Event with a value
     * @param name name of the event
     * @param contents description of the event
     * @param value value associated with event
     * @param hideFromFeed whether to hide the event from the client feed
     */
    public Event(String name, String contents, int value, boolean hideFromFeed){
        this.name = name;
        this.contents = contents;
        this.time = System.currentTimeMillis();
        this.hideFromFeed = hideFromFeed;
        this.value = value;
    }

    /**
     * Gets the name of the event
     * @return event name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets if it to be hidden from feed
     * @return true if hidden
     */
    public boolean isHidden(){
        return hideFromFeed;
    }

    /**
     * Gets the description/content of the event
     * @return description of event
     */
    public String getContents(){
        return contents;
    }

    /**
     * Gets the time the Event was created
     * @return time in milliseconds
     */
    public long getTime(){
        return time;
    }

    /**
     * Gets the formatted time
     * @return time formatted in "hh:mm:ss" format
     */
    public String getFormattedTime(){
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }

    /**
     * Gets the value associated with the event
     * @return value in event
     */
    public int getValue(){
       return value;
    }
}
