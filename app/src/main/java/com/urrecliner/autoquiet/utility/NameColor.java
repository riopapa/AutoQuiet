package com.urrecliner.autoquiet.utility;

public class NameColor {
    public static int get (String calName) {
        if (calName != null) {
            switch (calName) {
                case "Rio Papa":
                    return 0x8483B679;
                case "Rio Mama":
                    return 0x84e868ae;
                case "디지털교육":
                    return 0x844868ee;
            }
        }
        return 0x44333333;
    }
}
