package com.urrecliner.letmequiet;

import java.io.Serializable;

public class QuietTask implements Serializable {
    String subject;
    int startHour, startMin, finishHour, finishMin;
    boolean active, speaking, vibrate;
    boolean[] week = {true, true, true, true, true, true, true};

    QuietTask() { }

    QuietTask(String subject, int startHour, int startMin, int finishHour, int finishMin,
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
    void setSubject(String subject) { this.subject = subject; }
    void setActive(boolean TorF) { this.active = TorF; }

}
