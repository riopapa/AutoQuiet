package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

public class IsScreen {
    public static boolean On(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        boolean screenOn = false;
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                screenOn = true;
            }
        }
        return screenOn;
    }
}