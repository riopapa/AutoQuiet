package better.life.autoquiet.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NextTask implements Serializable {

    public String subject, caseSFOW;
    public int begHour, begMin;
    public long time;

    public NextTask() {}
}