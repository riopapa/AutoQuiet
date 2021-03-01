package com.urrecliner.letmequiet.models;

import java.io.Serializable;

public class QuietTask implements Serializable {
    String subject;
    int startHour, startMin, finishHour, finishMin;
    boolean active, speaking, vibrate;
    boolean[] week = {true, true, true, true, true, true, true};

    public QuietTask() { }

    public String getSubject() {
        return subject;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public int getFinishHour() {
        return finishHour;
    }

    public int getFinishMin() {
        return finishMin;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public boolean[] getWeek() {
        return week;
    }

    public QuietTask(String subject, int startHour, int startMin, int finishHour, int finishMin,
                     boolean[] week, boolean active, boolean vibrate, boolean speaking) {
        this.subject = subject;
        this.startHour = startHour;
        this.startMin = startMin;
        this.finishHour = finishHour;
        this.finishMin = finishMin;
        System.arraycopy(week, 0, this.week, 0, 7);
        this.active = active;
        this.vibrate = vibrate;
        this.speaking = speaking;
    }
    public void setSubject(String subject) { this.subject = subject; }
    public void setActive(boolean TorF) { this.active = TorF; }

}
