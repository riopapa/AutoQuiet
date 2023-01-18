package com.urrecliner.autoquiet.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class QuietTask implements Serializable {

    public boolean agenda;

    public String subject;
    public int startHour, startMin, finishHour, finishMin;
    public boolean active, vibrate;
    public boolean[] week = {true, true, true, true, true, true, true};
    public int sRepeatCount, fRepeatCount;

    public long calStartDate, calFinishDate;    // 통상으로 만들어지면 callStartDate = index 가 됨 sort 목적임
    public int calId;
    public String calDesc;
    public String calLocation;
    public String calName;
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
        this.agenda = false;
        System.arraycopy(week, 0, this.week, 0, 7);

    }
    
    // QuietTask from calendar
    public QuietTask(String title, long start, long finish, int id, String calName, String desc, String location, boolean active, boolean vibrate, int sRepeatCount, int fRepeatCount, boolean taskRepeat) {
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH", Locale.getDefault());
        SimpleDateFormat sdfMin = new SimpleDateFormat("mm", Locale.getDefault());
        this.agenda = true;
        this.subject = title;
        this.calStartDate = start;
        this.calFinishDate = finish;
        this.calId = id;
        this.calName = calName;
        this.calDesc = desc;
        this.calLocation = location;
        this.active = active;
        this.vibrate = vibrate;
        this.sRepeatCount = sRepeatCount;
        this.fRepeatCount = fRepeatCount;
        this.calTaskRepeat = taskRepeat;
        this.startHour = Integer.parseInt(sdfHour.format(start));
        this.startMin = Integer.parseInt(sdfMin.format(start));
        this.finishHour = Integer.parseInt(sdfHour.format(finish));
        this.finishMin =  Integer.parseInt(sdfMin.format(finish));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start);
        int weekNbr = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        this.week = new boolean[7];
        this.week[weekNbr] = true;
    }

    public void setSubject(String subject) { this.subject = subject; }
    public void setActive(boolean TorF) { this.active = TorF; }

}