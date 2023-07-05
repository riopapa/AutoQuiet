package com.urrecliner.autoquiet.models;

import com.urrecliner.autoquiet.R;
import com.urrecliner.autoquiet.Sub.Alarm99Icon;
import com.urrecliner.autoquiet.Sub.CalculateNext;

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
                long thisBeg = CalculateNext.calc(false, qThis.begHour, qThis.begMin, qThis.week, 0);
                if (thisBeg < nextTime) {
                    nextTime = thisBeg;
                    saveIdx = idx;
                    subject = qThis.subject;
                    begEnd = "S";
                    if (qThis.endHour != 99)
                        icon = (qThis.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
                    else
                        icon = new Alarm99Icon().getRscId(qThis.begLoop, qThis.endLoop);
                    soonOrUntil = "예정";
                }
                if (qThis.endHour != 99) {
                    long thisEnd = CalculateNext.calc(true, qThis.endHour, qThis.endMin, qThis.week, (qThis.begHour > qThis.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                    if (thisEnd < nextTime) {
                        nextTime = thisEnd;
                        saveIdx = idx;
                        subject = qThis.subject;
                        begEnd = (idx == 0) ? "O" : "F";
                        icon = (qThis.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
                        soonOrUntilN = "까지";
                    }
                }
            }
        }
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qNxt = quietTasks.get(idx);
            if (qNxt.active) {
                long nxtBeg = CalculateNext.calc(false, qNxt.begHour, qNxt.begMin, qNxt.week, 0);
                if (nxtBeg < nextTimeN && nxtBeg > nextTime) {
                    nextTimeN = nxtBeg;
                    begEndN = "S";
                    saveIdxN = idx;
                    subjectN = qNxt.subject;
                    if (qNxt.endHour != 99)
                        icon = (qNxt.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
                    else
                        icon = new Alarm99Icon().getRscId(qNxt.begLoop, qNxt.endLoop);
                    soonOrUntilN = "예정";
                }
                if (qNxt.endHour != 99) {
                    long nxtEnd = CalculateNext.calc(true, qNxt.endHour, qNxt.endMin, qNxt.week, (qNxt.begHour > qNxt.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                    if (nxtEnd < nextTimeN && nxtEnd > nextTime) {
                        nextTimeN = nxtEnd;
                        begEndN = (idx == 0) ? "O" : "F";
                        saveIdxN = idx;
                        subjectN = qNxt.subject;
                        iconN = (qNxt.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
                        soonOrUntilN = "까지";
                    }
                }
            }
        }
    }
}
