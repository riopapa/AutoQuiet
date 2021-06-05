package com.urrecliner.letmequiet.models;

import java.io.Serializable;

public class QuietTask implements Serializable {
    String subject;
    int startHour, startMin, finishHour, finishMin;
    boolean active, vibrate;
    boolean[] week = {true, true, true, true, true, true, true};
    int startRepeat, finishRepeat;
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
    public int getStartRepeat() { return startRepeat;}
    public int getFinishRepeat() { return finishRepeat;}

    public boolean gCalendar;
    public long calStartDate, calFinishDate;
    public int calId;
    public String calDesc;
    public String calLocation;

    public QuietTask(String subject, int startHour, int startMin, int finishHour, int finishMin,
                     boolean[] week, boolean active, boolean vibrate, int startRepeat, int finishRepeat) {
        this.subject = subject;
        this.startHour = startHour;
        this.startMin = startMin;
        this.finishHour = finishHour;
        this.finishMin = finishMin;
        this.active = active;
        this.vibrate = vibrate;
        this.startRepeat = startRepeat;
        this.finishRepeat = finishRepeat;
        this.gCalendar = false;
        System.arraycopy(week, 0, this.week, 0, 7);
    }
    public void setSubject(String subject) { this.subject = subject; }
    public void setActive(boolean TorF) { this.active = TorF; }

    public QuietTask(String subject, long start, long finish, int id, String desc, String location,
                     boolean active, boolean vibrate, int startRepeat, int finishRepeat) {
        this.gCalendar = true;
        this.subject = subject;
        this.calStartDate = start;
        this.calFinishDate = finish;
        this.calId = id;
        this.calDesc = desc;
        this.calLocation = location;
        this.active = active;
        this.vibrate = vibrate;
        this.startRepeat = startRepeat;
        this.finishRepeat = finishRepeat;
        System.arraycopy(week, 0, this.week, 0, 7);
    }

}
