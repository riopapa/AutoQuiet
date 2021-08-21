package com.urrecliner.autoquiet.utility;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import com.urrecliner.autoquiet.R;

public class NameColor {
    public static int get (String calName, Context context) {
        if (calName != null) {
            switch (calName) {
                case "디지털교육":
                    return ResourcesCompat.getColor(context.getResources(), R.color.nameDigital, null);
                case "events":
                    return ResourcesCompat.getColor(context.getResources(), R.color.nameEvents, null);
                case "Rio Papa":
                    return ResourcesCompat.getColor(context.getResources(), R.color.nameRioPapa, null);
                case "Rio Mama":
                    return ResourcesCompat.getColor(context.getResources(), R.color.nameRioMama, null);
            }
        }
        return ResourcesCompat.getColor(context.getResources(), R.color.nameOthers, null);
    }
}
