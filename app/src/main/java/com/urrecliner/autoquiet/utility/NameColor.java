package com.urrecliner.autoquiet.utility;

public class NameColor {
    public static int get (String calName) {
        switch (calName) {
            case "Rio Papa":
                return 0x446881e8;
            case "Rio Mama":
                return 0x44e868ae;
            case "디지털교육":
                return 0x444868ee;
        }
        return 0x44333333;
    }
}
