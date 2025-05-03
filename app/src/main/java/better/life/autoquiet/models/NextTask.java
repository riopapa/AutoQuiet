package better.life.autoquiet.models;

import java.io.Serializable;

public class NextTask implements Serializable {

    public String subject, caseSFOW, beginOrEnd;
    public int several, idx, alarmType, hour, min;
    public long time;
}