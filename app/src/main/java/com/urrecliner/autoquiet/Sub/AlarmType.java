package com.urrecliner.autoquiet.Sub;


import android.util.Log;

import com.urrecliner.autoquiet.R;
import static com.urrecliner.autoquiet.ActivityAddEdit.alarmIcons;

public class AlarmType {
    private int icon;
    private int [] iconSmall = {
            R.drawable.phone_normal, // 0
            R.drawable.phone_vibrate,   // 1
            R.drawable.phone_off, // 2
            R.drawable.bell_several,   // 3
            R.drawable.bell_tomorrow, // 4
            R.drawable.bell_onetime, // 5
            R.drawable.bell_once_gone  // 6 meaning less
    };

    public int getRscId(boolean end99, boolean vibrate, int begLoop, int endLoop) {
        if (end99) {
            if (begLoop == 0) {
                icon = 2;   // off, meaning less
            } else if (begLoop == 1) {
                if (endLoop == 0)
                    icon = 2;   // off, meaning less
                else if (endLoop == 1)
                    icon = 6;   // onetime and gone
                else
                    icon = 5;   // one time
            } else if (begLoop == 11) {
                if (endLoop == 0)
                    icon = 6;   // onetime and gone
                else if (endLoop == 1)
                    icon = 4;   // updated to tomorrow
                else
                    icon = 3;   //  several
            } else
                Log.w("resource", "id error "+end99+" "+begLoop+" "+endLoop);
        } else
            icon =  (vibrate) ? 1:2;

        return iconSmall[icon];
    }

    public int getDrawable(int alarmType) {
        return alarmIcons[alarmType];
    }
    public int getIcon(boolean end99, boolean vibrate, int begLoop, int endLoop) {
        int icon = getType(end99, vibrate, begLoop, endLoop);
        return alarmIcons[icon];
    }
    public int getType(boolean end99, boolean vibrate, int begLoop, int endLoop) {
        if (end99) {
            if (begLoop == 0) {
                icon = 0;   // off, meaning less
            } else if (begLoop == 1) {
                if (endLoop == 0)
                    icon = 0;   // off, meaning less
                else if (endLoop == 1)
                    icon = 4;   // onetime and gone
                else
                    icon = 3;   // one time
            } else if (begLoop == 11) {
                if (endLoop == 0)
                    icon = 4;   // onetime and gone
                else if (endLoop == 1)
                    icon = 2;   // updated to tomorrow
                else
                    icon = 1;   //  several
            } else
                Log.w("resource", "id error "+end99+" "+begLoop+" "+endLoop);
        } else
            icon = (vibrate) ? 5:6;
        return icon;
    }
}
