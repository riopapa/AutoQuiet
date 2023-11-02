package com.riopapa.autoquiet.models;

import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;

import com.riopapa.autoquiet.Sub.CalcNextBegEnd;

import java.util.ArrayList;

public class UpcomingTasks {

    public long nextTime, nextTimeN;
    public int saveIdx, saveIdxN, icon, iconN;
    public String subject, subjectN, begEnd, begEndN, soonOrUntil, soonOrUntilN;

    public UpcomingTasks(ArrayList<QuietTask> quietTasks) {

        long nowTime = System.currentTimeMillis() + 90000;
        nextTime = nowTime + 240*60*60*1000L;
        nextTimeN = nextTime;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qThis = quietTasks.get(idx);
            if (qThis.active) {
                CalcNextBegEnd cal = new CalcNextBegEnd(qThis);
                long thisBeg = cal.begTime;
                long thisEnd = cal.endTime;
//                long thisBeg = CalculateNext.calc(false, qThis.begHour, qThis.begMin, qThis.week, 0);
                if (thisBeg < nextTime && thisBeg > nowTime) {
                    nextTime = thisBeg;
                    saveIdx = idx;
                    subject = qThis.subject;
                    begEnd = "S";
                    icon = alarmIcons[qThis.alarmType];
                    soonOrUntil = "예정";
                }
                if (qThis.endHour == 99)
                    continue;
//                long thisEnd = CalculateNext.calc(true, qThis.endHour, qThis.endMin, qThis.week, (qThis.begHour > qThis.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
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
                CalcNextBegEnd cal = new CalcNextBegEnd(qNxt);
                long nxtBeg = cal.begTime;
                long nxtEnd = cal.endTime;
//                long nxtBeg = CalculateNext.calc(false, qNxt.begHour, qNxt.begMin, qNxt.week, 0);
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
//                long nxtEnd = CalculateNext.calc(true, qNxt.endHour, qNxt.endMin, qNxt.week, (qNxt.begHour > qNxt.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
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
