package com.urrecliner.letmequiet;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.urrecliner.letmequiet.Vars.addNewQuiet;
import static com.urrecliner.letmequiet.Vars.colorActive;
import static com.urrecliner.letmequiet.Vars.colorInactiveBack;
import static com.urrecliner.letmequiet.Vars.colorOff;
import static com.urrecliner.letmequiet.Vars.colorOffBack;
import static com.urrecliner.letmequiet.Vars.colorOn;
import static com.urrecliner.letmequiet.Vars.colorOnBack;
import static com.urrecliner.letmequiet.Vars.mainContext;
import static com.urrecliner.letmequiet.Vars.qIdx;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.utils;

public class RecycleViewAdapter  extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {

    static class MyViewHolder extends RecyclerView.ViewHolder {

        View viewLine;
        ImageView lvVibrate, lvSpeak;
        TextView rmdSubject, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
                tvStartTime, tvFinishTime, tvIdx;
        MyViewHolder (View view) {
            super(view);

            this.viewLine = itemView.findViewById(R.id.one_reminder);
            this.lvVibrate = itemView.findViewById(R.id.lv_vibrate);
            this.lvSpeak = itemView.findViewById(R.id.lv_speak);
            this.tvIdx = itemView.findViewById(R.id.idx);
            this.rmdSubject = itemView.findViewById(R.id.rmdSubject);
            this.ltWeek0 = itemView.findViewById(R.id.lt_week0);
            this.ltWeek1 = itemView.findViewById(R.id.lt_week1);
            this.ltWeek2 = itemView.findViewById(R.id.lt_week2);
            this.ltWeek3 = itemView.findViewById(R.id.lt_week3);
            this.ltWeek4 = itemView.findViewById(R.id.lt_week4);
            this.ltWeek5 = itemView.findViewById(R.id.lt_week5);
            this.ltWeek6 = itemView.findViewById(R.id.lt_week6);
            this.tvStartTime = itemView.findViewById(R.id.rmdStartTime);
            this.tvFinishTime = itemView.findViewById(R.id.rmdFinishTime);
            this.viewLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String idx = tvIdx.getText().toString();
                    qIdx = Integer.parseInt(idx);
                    quietTask = quietTasks.get(qIdx);
                    Intent intent;
                    if (qIdx != 0) {
                        addNewQuiet = false;
                        intent = new Intent(mainContext, AddUpdateActivity.class);
                    } else {
                        intent = new Intent(mainContext, OneTimeActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mainContext.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.reminder_info, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        qIdx = position;
        quietTask = quietTasks.get(qIdx);
        boolean active = quietTask.active;
        boolean vibrate = quietTask.vibrate;
        boolean speaking = quietTask.speaking;
        if (vibrate)
            holder.lvVibrate.setImageResource((active) ? R.mipmap.phone_vibrate :R.mipmap.speaking_noactive);
        else
            holder.lvVibrate.setImageResource((active) ? R.mipmap.phone_quiet : R.mipmap.speaking_noactive);
        if (speaking)
            holder.lvSpeak.setImageResource((active) ? R.mipmap.speaking_on :R.mipmap.speaking_noactive);
        else
            holder.lvSpeak.setImageResource((active) ? R.mipmap.speaking_off : R.mipmap.speaking_noactive);

        holder.rmdSubject.setText(quietTask.subject);
        holder.rmdSubject.setTextColor((active) ? colorOn:colorOff);

        TextView[] tViewWeek = new TextView[7];
        tViewWeek[0] = holder.ltWeek0; tViewWeek[1] = holder.ltWeek1; tViewWeek[2] = holder.ltWeek2;
        tViewWeek[3] = holder.ltWeek3; tViewWeek[4] = holder.ltWeek4; tViewWeek[5] = holder.ltWeek5;
        tViewWeek[6] = holder.ltWeek6;
        if (qIdx == 0) {
            for (int i = 0; i < 7; i++) {
                tViewWeek[i].setTextColor(colorOffBack);  // transparent
            }
            String txt = "-";
            holder.tvStartTime.setText(txt);
            holder.tvStartTime.setTextColor((active) ? colorOn:colorOff);
            txt = utils.hourMin(quietTask.finishHour, quietTask.finishMin);
            holder.tvFinishTime.setText(txt);
        }
        else{
            boolean[] week = quietTask.week;
            for (int i = 0; i < 7; i++) {
                tViewWeek[i].setTextColor(week[i] ? colorActive : colorOff);
                if (active)
                    tViewWeek[i].setBackgroundColor(week[i] ? colorOnBack : colorOffBack);
                else
                    tViewWeek[i].setBackgroundColor(week[i] ? colorInactiveBack : colorOffBack);
            }
            String txt = utils.hourMin (quietTask.startHour, quietTask.startMin);
            holder.tvStartTime.setText(txt);
            holder.tvStartTime.setTextColor((active) ? colorOn:colorOff);
            txt = utils.hourMin (quietTask.finishHour, quietTask.finishMin);
            holder.tvFinishTime.setText(txt);
        }
        holder.tvFinishTime.setTextColor((active) ? colorOn:colorOff);
        holder.tvIdx.setText(""+position);
//        int diff = position * 16;
//        holder.view.setBackgroundColor(ContextCompat.getColor(mainContext, R.color.line_item_back) + diff + diff*256 + diff*256*256);
    }

    @Override
    public int getItemCount(){
        return quietTasks.size();
    }
}
