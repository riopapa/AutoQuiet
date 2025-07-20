package better.life.autoquiet.activity;

import static better.life.autoquiet.activity.ActivityMain.vars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import better.life.autoquiet.QuietTaskGetPut;
import better.life.autoquiet.databinding.ActivityAddAgendaBinding;
import better.life.autoquiet.models.GCal;
import better.life.autoquiet.calendar.GetAgenda;
import better.life.autoquiet.Sub.NameColor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ActivityAddAgenda extends AppCompatActivity {

    private GCal gCal;
    private ActivityAddAgendaBinding binding;
    private String mTitle;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddAgendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        QuietTaskGetPut.get();
        ArrayList<GCal> gCals = new GetAgenda().get(this);
        Intent intent = getIntent();
        int currIdx = intent.getExtras().getInt("idx", -1);
        gCal = gCals.get(currIdx);
        show_OneAgenda();
    }

    void show_OneAgenda() {

        final SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE) ", Locale.getDefault());
        final SimpleDateFormat sdfHourMin = new SimpleDateFormat("HH:mm ", Locale.getDefault());
        binding.aTitle.setText(gCal.title);
        mTitle = gCal.title;

        binding.aTitle.setBackgroundColor(NameColor.get(gCal.calName, context));
        String s = sdfDate.format(gCal.begTime)+ sdfHourMin.format(gCal.begTime)
                +" ~ "+ sdfHourMin.format(gCal.endTime);
        binding.aDate.setText(s);
        binding.acalName.setText(gCal.calName);
        binding.acalName.setBackgroundColor(NameColor.get(gCal.calName, context));
        binding.aLocation.setText(gCal.location);
        binding.aDesc.setText(gCal.desc);
        binding.aRepeat.setText((gCal.repeat)? gCal.rule:"");

        String []dispName = new String[] { "10분전", "9분전", "8분전", "7분전", "6분전",
                "5분전",  "4분전", "3분전",  "2분전", "1분전",  " 정시 ", "1분후",  "2분후",  "3분후",  "4분후",
                "5분후", "6분후", "7분후", "8분후", "9분후", "10분후" };
        binding.aBefore.setMaxValue(20);
        binding.aBefore.setMinValue(0);
        binding.aBefore.setWrapSelectorWheel(false);
        binding.aBefore.setDisplayedValues(dispName);
        binding.aBefore.setValue(Integer.parseInt(vars.sharedTimeBefore)+10);
        binding.aBefore.setTextSize(60);
        binding.aAfter.setMaxValue(20);
        binding.aAfter.setMinValue(0);
        binding.aAfter.setWrapSelectorWheel(false);
        binding.aAfter.setDisplayedValues(dispName);
        binding.aAfter.setValue(Integer.parseInt(vars.sharedTimeAfter)+10);
        binding.aAfter.setTextSize(60);
        binding.aAdd.setOnClickListener(v -> {
            mTitle = binding.aTitle.getText().toString();
            int before = binding.aBefore.getValue() - 10;
            int after = binding.aAfter.getValue() - 10;
            addAgenda(gCal.id, before, after);
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(binding.aTitle.getWindowToken(), 0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    void addAgenda(int id, int before, int after) {

//        SimpleDateFormat sdfDateHour = new SimpleDateFormat("MM-dd(EEE) HH:mm", Locale.getDefault());
//        StringBuilder sb  = new StringBuilder();
//        sb.append("Following Tasks Added");
//        int qId = (int) (gCal.begTime & 0x7ffffff);
//        for (int i = 0; i < gCals.size(); i++) {    // if repeat item add all
//            if (gCals.get(i).id == id) {
//                GCal gC = gCals.get(i);
//                QuietTask q = new QuietTask(mTitle, gC.begTime + (long) before * 60 * 1000, gC.endTime + (long) after * 60 * 1000,
//                        qId, gC.calName, gC.desc, gC.location, true, 5, gC.repeat);
//                sb.append("\n").append(q.subject).append(" ").append(sdfDateHour.format(q.calBegDate));
//                quietTasks.add(q);
//            }
//        }
//        Toast.makeText(context, sb, Toast.LENGTH_LONG).show();
//        QuietTaskGetPut.put();
//        VarsGetPut.put(vars, context);
//        finish();
    }
}