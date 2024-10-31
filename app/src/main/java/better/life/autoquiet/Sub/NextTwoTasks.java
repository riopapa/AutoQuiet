package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_WORK;
import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.ScheduleNextTask.AHEAD_TIME;

import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class NextTwoTasks {

    public long nextTime, nextTimeN;
    public int saveIdx, icon, iconN, sHour, sHourN, sMin, sMinN, several;
    public String subject, subjectN, caseSFOW, beginOrEnd, beginOrEndN;
    QuietTask qt;

    SimpleDateFormat sdfDateTime = new SimpleDateFormat("MM-dd(EEE) HH:mm", Locale.getDefault());

    public NextTwoTasks(ArrayList<QuietTask> quietTasks) {

        ArrayList<String> nextTasks = new ArrayList<>();

        final long nowTime = System.currentTimeMillis();
        long nxtStart, nxtFinish;

        for (int idx = 0; idx < quietTasks.size(); idx++) {
            qt = quietTasks.get(idx);
            if (qt.active) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1); // calculate from yesterday
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                int WKStart = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday
                for (int wkNbr = WKStart; ; ) {
                    if (qt.week[wkNbr]) {
                        cal.set(Calendar.HOUR_OF_DAY, qt.begHour);
                        cal.set(Calendar.MINUTE, qt.begMin);
                        nxtStart = cal.getTimeInMillis();
                        if (qt.endHour == 99)
                            nxtFinish = 0;
                        else {
                            Calendar cal2 = (Calendar) cal.clone();
                            cal2.set(Calendar.HOUR_OF_DAY, qt.endHour);
                            cal2.set(Calendar.MINUTE, qt.endMin);
                            if (qt.begHour > qt.endHour) {
                                cal2.add(Calendar.DATE, 1);
                            }
                            nxtFinish = cal2.getTimeInMillis();
                        }
                        if (nowTime < nxtStart || nowTime < nxtFinish)
                            break;
                    }
                    wkNbr++;
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    if (wkNbr == 7)
                        wkNbr = 0;
                }

                if (nowTime < nxtStart) {
                    if (qt.alarmType == BELL_SEVERAL) {
                        nxtStart -= AHEAD_TIME + AHEAD_TIME + AHEAD_TIME;
                        several = 2;
                    } else
                        several = 0;
                    String [] s = new String[] {sdfDateTime.format(nxtStart), nxtStart + "",
                            "S", qt.subject, several+"", alarmIcons[qt.alarmType] + "",
                            idx+"", qt.begHour + "", qt.begMin+"","시작"};
                    String s0 = String.join("_", s);
                    nextTasks.add(String.join("_", s0));
                }
                if (qt.endHour != 99 && nowTime < nxtFinish) {
                    several = (qt.sayDate) ? 3 : 0;
                    String wf = (qt.alarmType == PHONE_WORK) ? "W":"F";  // Work, Finish
                    String [] s = new String[] {sdfDateTime.format(nxtFinish), nxtFinish + "",
                            (idx == 0) ? "O" : wf, qt.subject, several+"", alarmIcons[qt.alarmType] + "",
                            idx+"", qt.endHour + "", qt.endMin + "","까지"};
                    String s0 = String.join("_", s);
                    nextTasks.add(String.join(" ", s0));
                }
            }
        }
        Collections.sort(nextTasks);
        // check whether remove first task
        String[] sFirst, sSecond;   // 1730289363612 9
        while (true) {
            sFirst = nextTasks.get(0).split("_");
            nextTime = Long.parseLong(sFirst[1]);
            if (nextTime < nowTime) {
                nextTasks.remove(0);
            } else
                break;
        }
        sSecond = nextTasks.get(1).split("_");
        nextTimeN = Long.parseLong(sSecond[1]);
        caseSFOW = sFirst[2];
        subject = sFirst[3]; subjectN = sSecond[3];
        several = Integer.parseInt(sFirst[4]);  // no several from sSecond
        icon = Integer.parseInt(sFirst[5]);  iconN = Integer.parseInt(sSecond[5]);
        saveIdx = Integer.parseInt(sFirst[6]);
        sHour = Integer.parseInt(sFirst[7]);  sHourN = Integer.parseInt(sSecond[7]);
        sMin = Integer.parseInt(sFirst[8]);  sMinN = Integer.parseInt(sSecond[8]);
        beginOrEnd = sFirst[9]; beginOrEndN = sSecond[9];
    }
}
