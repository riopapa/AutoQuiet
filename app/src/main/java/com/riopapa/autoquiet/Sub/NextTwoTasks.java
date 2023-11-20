package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.ScheduleNextTask.AHEAD_TIME;

import android.util.Log;

import com.riopapa.autoquiet.models.QuietTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class NextTwoTasks {

    public long nextTime, nextTimeN;
    public int saveIdx, icon, iconN, sHour, sHourN, sMin, sMinN, several;
    public String subject, subjectN, caseSFO, beginOrEnd, beginOrEndN;
    QuietTask qt;
    public NextTwoTasks(ArrayList<QuietTask> quietTasks) {

        ArrayList<String> nextTasks = new ArrayList<>();

        long nowTime = System.currentTimeMillis() + AHEAD_TIME;
        long nxtStart, nxtFinish;

        for (int idx = 0; idx < quietTasks.size(); idx++) {
            qt = quietTasks.get(idx);
            if (qt.active) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                int WK = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday

                for (int i = WK; ; ) {
                    if (qt.week[i]) {
                        cal.set(Calendar.HOUR_OF_DAY, qt.begHour);
                        cal.set(Calendar.MINUTE, qt.begMin);
                        nxtStart = cal.getTimeInMillis();
                        cal.set(Calendar.HOUR_OF_DAY, qt.endHour);
                        cal.set(Calendar.MINUTE, qt.endMin);
                        if (qt.endHour == 99)
                            nxtFinish = 0;
                        else {
                            if (qt.begHour > qt.endHour)
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            nxtFinish = cal.getTimeInMillis();
                        }
                        break;
                    }
                    i++;
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    if (i == 7)
                        i = 0;
                }

                if (nowTime < nxtStart) {
                    if (qt.alarmType == BELL_SEVERAL) {
                        nxtStart -= AHEAD_TIME + AHEAD_TIME + AHEAD_TIME;
                        several = 3;
                    } else
                        several = 0;
                    String s = nxtStart + "_" + "S_ " + qt.subject + "_" +
                            several + "_" + alarmIcons[qt.alarmType] + "_" +
                            idx + "_" + qt.begHour + "_" + qt.begMin + "_" +
                            "시작";
                    nextTasks.add(s);
                }
                if (qt.endHour != 99 && nowTime < nxtFinish) {
                    several = (qt.sayDate) ? 3 : 0;
                    String s = nxtFinish + "_" + ((idx == 0) ? "O_ " : "F_") + qt.subject + "_" +
                            several + "_" + alarmIcons[qt.alarmType] + "_" +
                            idx + "_" + qt.endHour + "_" + qt.endMin + "_" +
                            "까지";
                    nextTasks.add(s);
                }
            }
        }
        Collections.sort(nextTasks);

        String [] sFirst = nextTasks.get(0).split("_");
        String [] sSecond = nextTasks.get(1).split("_");

        nextTime = Long.parseLong(sFirst[0]);   nextTimeN = Long.parseLong(sSecond[0]);
        caseSFO = sFirst[1];
        subject = sFirst[2]; subjectN = sSecond[2];
        several = Integer.parseInt(sFirst[3]);  // no several from sSecond
        icon = Integer.parseInt(sFirst[4]);  iconN = Integer.parseInt(sSecond[4]);
        saveIdx = Integer.parseInt(sFirst[5]);
        sHour = Integer.parseInt(sFirst[6]);  sHourN = Integer.parseInt(sSecond[6]);
        sMin = Integer.parseInt(sFirst[7]);  sMinN = Integer.parseInt(sSecond[7]);
        beginOrEnd = sFirst[8]; beginOrEndN = sSecond[8];
    }
}
