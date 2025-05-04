package better.life.autoquiet.models;

import java.io.Serializable;

public class NextTask implements Serializable {

    public String timeS, subject, SFO, suffix;
    public int several, idx, alarmType, hour, min;
    public boolean vibrate, sayDate, clock;
    public long time;
}