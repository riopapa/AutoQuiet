package better.life.autoquiet.Sub;

import android.annotation.SuppressLint;
import android.content.Context;

public class ContextProvider {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static Context get() {
        return context;
    }
}
