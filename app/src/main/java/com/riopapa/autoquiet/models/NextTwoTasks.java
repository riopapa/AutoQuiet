package com.riopapa.autoquiet.models;

import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;

import com.riopapa.autoquiet.Sub.CalculateNext;

import java.util.ArrayList;

public class NextTwoTasks {

    public long nextTime, nextTimeN;
    public int saveIdx, saveIdxN, icon, iconN;
    public String subject, subjectN, begEnd, begEndN, soonOrUntil, soonOrUntilN;

    public NextTwoTasks(ArrayList<QuietTask> quietTasks) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        nextTimeN = nextTime;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qThis = quietTasks.get(idx);
            if (qThis.active) {
//                if (qThis.alarmType == 0) {
//                    qThis.alarmType = new AlarmType().getType(
//                            qThis.endHour == 99, qThis.vibrate, qThis.begLoop, qThis.endLoop);
//                    quietTasks.set(idx, qThis);
//                    new QuietTaskGetPut().put(quietTasks);
//                }
                long thisBeg = CalculateNext.calc(false, qThis.begHour, qThis.begMin, qThis.week, 0);
                if (thisBeg < nextTime) {
                    nextTime = thisBeg;
                    saveIdx = idx;
                    subject = qThis.subject;
                    begEnd = "S";
                    icon = alarmIcons[qThis.alarmType];
                    soonOrUntil = "예정";
                }
                if (qThis.endHour == 99)
                    continue;
                long thisEnd = CalculateNext.calc(true, qThis.endHour, qThis.endMin, qThis.week, (qThis.begHour > qThis.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                if (thisEnd < nextTime) {
                    nextTime = thisEnd;
                    saveIdx = idx;
                    subject = qThis.subject;
                    begEnd = (idx == 0) ? "O" : "F";
                    icon = alarmIcons[qThis.alarmType];
                    soonOrUntilN = "까지";
                }
            }
        }
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qNxt = quietTasks.get(idx);
            if (qNxt.active) {
//                if (qNxt.alarmType == 0) {
//                    qNxt.alarmType = new AlarmType().getType(
//                            qNxt.endHour == 99, qNxt.vibrate, qNxt.begLoop, qNxt.endLoop);
//                    quietTasks.set(idx, qNxt);
//                    new QuietTaskGetPut().put(quietTasks);
//                }
                long nxtBeg = CalculateNext.calc(false, qNxt.begHour, qNxt.begMin, qNxt.week, 0);
                if (nxtBeg < nextTimeN && nxtBeg > nextTime) {
                    nextTimeN = nxtBeg;
                    begEndN = "S";
                    saveIdxN = idx;
                    subjectN = qNxt.subject;
                    iconN = alarmIcons[qNxt.alarmType];
                    soonOrUntilN = "예정";
                }
                if (qNxt.endHour == 99)
                    continue;
                long nxtEnd = CalculateNext.calc(true, qNxt.endHour, qNxt.endMin, qNxt.week, (qNxt.begHour > qNxt.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                if (nxtEnd < nextTimeN && nxtEnd > nextTime) {
                    nextTimeN = nxtEnd;
                    begEndN = (idx == 0) ? "O" : "F";
                    saveIdxN = idx;
                    subjectN = qNxt.subject;
                    iconN = alarmIcons[qNxt.alarmType];
                    soonOrUntilN = "까지";
                }
            }
        }
    }
}