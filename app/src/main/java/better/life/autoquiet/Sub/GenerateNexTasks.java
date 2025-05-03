package better.life.autoquiet.Sub;

import static better.life.autoquiet.ScheduleNextTask.AHEAD_TIME;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import java.util.ArrayList;
import java.util.Calendar;

import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;

public class GenerateNexTasks {

    QuietTask qt;
    int several;

    void gen(ArrayList<QuietTask> quietTasks) {
        ArrayList <NextTask> nTs;
        nTs = new ArrayList<>();
        final long nowTime = System.currentTimeMillis()+ 30000;
        final long farTime = nowTime + 36*60*60*1000;
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
                    NextTask nt = new NextTask();
                    nt.time = nxtStart;
                    nt.caseSFOW = "S";
                    nt.subject = qt.subject;
                    nt.several = several;
                    nt.alarmType = qt.alarmType;
                    nt.idx = idx;
                    nt.hour = qt.begHour;
                    nt.min = qt.begMin;
                    nt.beginOrEnd = "시작";
                    nTs.add(nt);
                }
                if (qt.endHour != 99 && nowTime < nxtFinish) {
                    several = (qt.sayDate) ? 3 : 0;
                    NextTask nt = new NextTask();
                    nt.time = nxtFinish;
                    nt.caseSFOW = "S";
                    nt.subject = qt.subject;
                    nt.several = several;
                    nt.alarmType = qt.alarmType;
                    nt.idx = idx;
                    nt.hour = qt.begHour;
                    nt.min = qt.begMin;
                    nt.beginOrEnd = "까지";
                    nTs.add(nt);
                }
            }
        }

        nextTasks = new ArrayList<>();
        for (NextTask nt: nTs) {
            if (nt.time > nowTime && nt.time < farTime)
                nextTasks.add(nt);
        }
        nextTasks.sort((nt1, nt2) -> Long.compare(nt1.time, nt2.time));
    }

}
