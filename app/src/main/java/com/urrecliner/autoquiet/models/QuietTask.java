package com.urrecliner.autoquiet.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class QuietTask implements Serializable {

    public boolean agenda;

    public String subject;
    public int begHour, begMin;
    public int endHour, endMin;
    // if endHour == 99 알람 정보를 소리내는 용도
    public boolean active;  // 해당 task 를 잠시 죽일 떄 사용
    public boolean vibrate; // 폰을 진동할찌 여부
    public boolean sayDate; // 현재 일자, 시각을 말할찌 여부

    public boolean[] week = {true, true, true, true, true, true, true};
    public int begLoop, endLoop;  // 0 : not Active, 1: bell only, > 1: talk subject
    // begLoop = 0 무음 보드, endLoop == 1 삐 소리만,  > 1 subject를 읽어 주는 것
    public long calBegDate, calEndDate;    // 통상으로 만들어지면 callStartDate = index 가 됨 sort 목적임
    public int calId;
    public String calDesc;
    public String calLocation;
    public String calName;
    public boolean calTaskRepeat;

    public long sortKey;    // 0번째 바로 조용히 는 -1

    // normal QuietTask
    public QuietTask(String subject, int begHour, int begMin, int endHour, int endMin, boolean[] week, boolean active, boolean vibrate, int begLoop, int endLoop, boolean sayDate) {
        this.subject = subject;
        this.begHour = begHour;
        this.begMin = begMin;
        this.endHour = endHour;
        this.endMin = endMin;
        this.active = active;
        this.vibrate = vibrate;
        this.begLoop = begLoop;
        this.endLoop = endLoop;
        this.agenda = false;
        this.sayDate = sayDate;
        System.arraycopy(week, 0, this.week, 0, 7);
    }
    
    // QuietTask from calendar
    public QuietTask(String title, long beg, long end, int id, String calName, String desc, String location, boolean active, boolean vibrate, int begLoop, int endLoop, boolean taskRepeat) {
        SimpleDateFormat sdfHour = new SimpleDateFormat("HH", Locale.getDefault());
        SimpleDateFormat sdfMin = new SimpleDateFormat("mm", Locale.getDefault());
        this.agenda = true;
        this.subject = title;
        this.calBegDate = beg;
        this.calEndDate = end;
        this.calId = id;
        this.calName = calName;
        this.calDesc = desc;
        this.calLocation = location;
        this.active = active;
        this.vibrate = vibrate;
        this.begLoop = begLoop;
        this.endLoop = endLoop;
        this.calTaskRepeat = taskRepeat;
        this.begHour = Integer.parseInt(sdfHour.format(beg));
        this.begMin = Integer.parseInt(sdfMin.format(beg));
        this.endHour = Integer.parseInt(sdfHour.format(end));
        this.endMin =  Integer.parseInt(sdfMin.format(end));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(beg);
        int weekNbr = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        this.week = new boolean[7];
        this.week[weekNbr] = true;
        this.sayDate = true;
    }
}