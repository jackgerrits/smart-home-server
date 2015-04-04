package com.jackgerrits.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jack on 21/03/2015.
 */
public class Event {
    private String name;
    private String contents;
    private long time;
    boolean hideFromFeed;

    public Event(String name, String contents, boolean hideFromFeed){
        this.name = name;
        this.contents = contents;
        this.time = System.currentTimeMillis();
        this.hideFromFeed = hideFromFeed;
    }

    public String getName(){
        return name;
    }

    public boolean isHidden(){
        return hideFromFeed;
    }

    public String getContents(){
        return contents;
    }

    public long getTime(){
        return time;
    }

    public String getFormattedTime(){
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }


}
