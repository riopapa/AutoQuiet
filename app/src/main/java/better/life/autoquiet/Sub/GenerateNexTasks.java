package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;

public class GenerateNexTasks {

    QuietTask qt;
    int several;
    ArrayList <NextTask> nTs;

    public void gen(ArrayList<QuietTask> quietTasks) {
        nTs = new ArrayList<>();
        final long nowTime = System.currentTimeMillis()+ 30000;
        final long farTime = nowTime + 30*60*60*1000;

        for (int idx = 0; idx < quietTasks.size(); idx++) {
            qt = quietTasks.get(idx);
            if (!qt.active)
                continue;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1); // calculate from yesterday
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int WKStart = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday
            int WKFinish = WKStart + 3;
            boolean[] twoWeeks = new boolean[14];
            System.arraycopy(qt.week, 0, twoWeeks, 0, 7);
            System.arraycopy(qt.week, 0, twoWeeks, 7, 7);
            cal.set(Calendar.HOUR_OF_DAY, qt.begHour);
            cal.set(Calendar.MINUTE, qt.begMin);

            for (int wkNbr = WKStart; wkNbr < WKFinish; wkNbr++) {
                if (twoWeeks[wkNbr]) {
                    add2NextTasks(idx,"S", cal, qt);
                    if (qt.endHour != 99) {
                        Calendar cal2 = (Calendar) cal.clone();
                        cal2.set(Calendar.HOUR_OF_DAY, qt.endHour);
                        cal2.set(Calendar.MINUTE, qt.endMin);
                        if (qt.begHour > qt.endHour) {
                            cal2.add(Calendar.DATE, 1);
                        }
                        add2NextTasks(idx,"F", cal2, qt);
                    }
                }
                cal.add(Calendar.DATE, 1);
            }
        }

        nextTasks = new ArrayList<>();
        for (NextTask nt: nTs) {
            if (nt.time > nowTime && nt.time < farTime)
                nextTasks.add(nt);
        }
        nextTasks.sort(Comparator.comparingLong(nt -> nt.time));
    }

    private void add2NextTasks(int idx, String SFO, Calendar cal, QuietTask qt) {
        long setTime = cal.getTimeInMillis();
        if (qt.alarmType == BELL_SEVERAL) {
            setTime += 12000;
            several = 2;
        } else
            several = 0;
        NextTask nt = new NextTask();
        nt.time = setTime;
        nt.SFO = SFO;
        nt.subject = qt.subject;
        nt.several = several;
        nt.alarmType = qt.alarmType;
        nt.idx = idx;
        if (SFO.equals("F")) {
            nt.hour = qt.endHour;
            nt.min = qt.endMin;
            if (qt.endHour != 99)
                nt.suffix = "까지";
        } else {
            nt.hour = qt.begHour;
            nt.min = qt.begMin;
            if (qt.endHour != 99)
                nt.suffix = "시작";
            else
                nt.suffix = "";
        }
        nt.vibrate = qt.vibrate;
        nt.sayDate = qt.sayDate;
        nt.clock = qt.clock;
        nTs.add(nt);
    }

}
