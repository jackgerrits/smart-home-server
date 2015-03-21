package com.jackgerrits;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jack on 21/03/2015.
 */
public class Event {
    String name;
    String contents;
    long time;

    public Event(String name, String contents){
        this.name = name;
        this.contents = contents;
        this.time = System.currentTimeMillis();
    }

    public String getName(){
        return name;
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
