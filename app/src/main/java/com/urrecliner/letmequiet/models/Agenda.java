package com.urrecliner.letmequiet.models;

public class Agenda {

    public int id;              // 0
    public String title;        // 1
    public String desc;         // 2
    public long startTime;      // 3
    public long finishTime;     // 4
    public String location;     // 5
    public boolean isAllDay;    // 6
    public String calName;      // 7
    public String timeZone;     // 8
    public String rule;         // 9

    public int quietStart, quietFinish;
    public long getStartTime() { return startTime; }
}
