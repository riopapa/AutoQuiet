package better.life.autoquiet.models;

import java.io.Serializable;

public class NextTask implements Serializable {

    public long time;
    public String subject, SFO, suffix;
    public int several, idx, alarmType, hour, min;
    public boolean vibrate, sayDate, clock;
}