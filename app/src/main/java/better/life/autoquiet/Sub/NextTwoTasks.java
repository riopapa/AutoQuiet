package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class NextTwoTasks {

    public long nextTime, nextTimeN;
    public int idx, icon, iconN, sHour, sHourN, sMin, sMinN, several;
    public String subject, subjectN, caseSFOW, beginOrEnd, beginOrEndN;

    public NextTwoTasks(ArrayList<QuietTask> quietTasks) {

        new GenerateNexTasks().gen(quietTasks);
        NextTask nt1 = nextTasks.get(0);
        NextTask nt2 = nextTasks.get(1);
        nextTime = nt1.time;
        caseSFOW = nt1.caseSFOW;
        subject = nt1.subject;
        several = nt1.several;  // no several from sSecond
        icon = alarmIcons[nt1.alarmType];
        beginOrEnd = nt1.beginOrEnd;
        sHour = nt1.hour;
        sMin = nt1.min;

        nextTimeN = nt2.time;
        subjectN = nt2.subject;
        iconN = alarmIcons[nt2.alarmType];
        sHourN = nt2.hour;
        sMinN = nt2.min;
        beginOrEndN = nt2.beginOrEnd;
    }

}
