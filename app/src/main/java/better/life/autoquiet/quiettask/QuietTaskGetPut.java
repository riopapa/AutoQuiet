package better.life.autoquiet.quiettask;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.models.QuietTask;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuietTaskGetPut {
    public void put(ArrayList<QuietTask> quietTasks) {
        Context context = ContextProvider.get();

        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("silentInfo", json);
        prefsEditor.apply();
    }

    public ArrayList<QuietTask> get() {

        Context context = ContextProvider.get();
        if (context == null)
            Log.e("getput", "////// context is null -------");
        ArrayList<QuietTask> list;

        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("silentInfo", "");
        if (json.isEmpty()) {
            list = new ArrayList<>();
            new QuietTaskNew();
        } else {
            Type type = new TypeToken<List<QuietTask>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

}