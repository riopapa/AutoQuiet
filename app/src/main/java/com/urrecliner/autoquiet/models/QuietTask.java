package com.urrecliner.autoquiet.models;

import java.io.Serializable;

public class QuietTask implements Serializable {

    public boolean gCalendar;

    String subject;
    int startHour, startMin, finishHour, finishMin;
    boolean active, vibrate;
    boolean[] week = {true, true, true, true, true, true, true};
    int sRepeatCount, fRepeatCount;

    public long calStartDate, calFinishDate;
    public int calId;
    public String calDesc;
    public String calLocation;
    public boolean calTaskRepeat;

    // normal QuietTask
    public QuietTask(String subject, int startHour, int startMin, int finishHour, int finishMin, boolean[] week, boolean active, boolean vibrate, int sRepeatCount, int fRepeatCount) {
        this.subject = subject;
        this.startHour = startHour;
        this.startMin = startMin;
        this.finishHour = finishHour;
        this.finishMin = finishMin;
        this.active = active;
        this.vibrate = vibrate;
        this.sRepeatCount = sRepeatCount;
        this.fRepeatCount = fRepeatCount;
        this.gCalendar = false;
        System.arraycopy(week, 0, this.week, 0, 7);

    }
    
    // QuietTask from calendar
    public QuietTask(String title, long start, long finish, int id, String desc, String location, boolean active, boolean vibrate, int sRepeatCount, int fRepeatCount, boolean taskRepeat) {
        this.gCalendar = true;
        this.subject = title;
        this.calStartDate = start;
        this.calFinishDate = finish;
        this.calId = id;
        this.calDesc = desc;
        this.calLocation = location;
        this.active = active;
        this.vibrate = vibrate;
        this.sRepeatCount = sRepeatCount;
        this.fRepeatCount = fRepeatCount;
        this.calTaskRepeat = taskRepeat;
    }

    public String getSubject() { return subject;}
    public int getStartHour() {return startHour;}
    public int getStartMin() {return startMin;}
    public int getFinishHour(){
        return finishHour;
    }
    public int getFinishMin() {
        return finishMin;
    }
    public boolean isActive() {
        return active;
    }
    public boolean isVibrate() { return vibrate;}
    public boolean[] getWeek() { return week;}
    public int getsRepeatCount() { return sRepeatCount;}
    public int getfRepeatCount() { return fRepeatCount;}

    public void setSubject(String subject) { this.subject = subject; }
    public void setActive(boolean TorF) { this.active = TorF; }

}
