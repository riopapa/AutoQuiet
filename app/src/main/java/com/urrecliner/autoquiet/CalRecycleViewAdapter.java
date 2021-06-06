package com.urrecliner.autoquiet;

import android.content.Intent;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.urrecliner.autoquiet.models.Agenda;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.ItemTouchHelperAdapter;

import java.text.SimpleDateFormat;

import static com.urrecliner.autoquiet.Vars.addNewQuiet;
import static com.urrecliner.autoquiet.Vars.agendas;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

public class CalRecycleViewAdapter extends RecyclerView.Adapter<CalRecycleViewAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private ItemTouchHelper mTouchHelper;
    private Agenda agenda;
    private int colorOn, colorOnBack, colorInactiveBack, colorOff, colorOffBack, colorActive;
    private int topLine = -1;
    private View swipeView;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        colorOn = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOn, null);
        colorInactiveBack = ResourcesCompat.getColor(mContext.getResources(), R.color.colorInactiveBack, null);
        colorOnBack = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOnBack, null);
        colorOff = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOff, null);
        colorActive = ResourcesCompat.getColor(mContext.getResources(), R.color.colorActive, null);
        colorOffBack = ResourcesCompat.getColor(mContext.getResources(), R.color.colorOffBack, null);

        swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_line, parent, false);

        return new ViewHolder(swipeView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            GestureDetector.OnGestureListener
    {

        View viewLine;
        ImageView lvVibrate, lvStartRepeat, lvFinishRepeat;
        TextView rmdSubject, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
                tvStartTime, tvFinishTime;
        GestureDetector mGestureDetector;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_reminder);
            this.lvVibrate = itemView.findViewById(R.id.lv_vibrate);
            this.lvStartRepeat = itemView.findViewById(R.id.lv_startRepeat);
            this.lvFinishRepeat = itemView.findViewById(R.id.lv_finishRepeat);
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
            this.viewLine.setOnClickListener(v -> {
                int qIdx = getBindingAdapterPosition();
                agenda = agendas.get(qIdx);
                Intent intent;
                if (qIdx != 0) {
                    addNewQuiet = false;
                    intent = new Intent(mContext, AddUpdateActivity.class);
                } else {
                    intent = new Intent(mContext, OneTimeActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            });
            mGestureDetector = new GestureDetector(itemView.getContext(), this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) { }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int qIdx = getBindingAdapterPosition();
            agenda = agendas.get(qIdx);
            Intent intent;
            if (qIdx != 0) {
                addNewQuiet = false;
                intent = new Intent(mContext, AddUpdateActivity.class);
            } else {
                intent = new Intent(mContext, OneTimeActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("idx",qIdx);
            mContext.startActivity(intent);

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        agenda = agendas.get(position);
        boolean active = true;;
        boolean vibrate = true;;
        int startRepeat = agenda.quietStart;
        int finishRepeat = agenda.quietFinish;
        if (vibrate)
            holder.lvVibrate.setImageResource((active) ? R.mipmap.phone_vibrate : R.mipmap.speaking_noactive);
        else
            holder.lvVibrate.setImageResource((active) ? R.mipmap.phone_quiet : R.mipmap.speaking_noactive);
        holder.lvStartRepeat.setImageResource((startRepeat == 0)? R.mipmap.speaking_off: (startRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);
        holder.lvFinishRepeat.setImageResource((finishRepeat == 0)? R.mipmap.speaking_off: (finishRepeat == 1)? R.mipmap.speaking_on : R.mipmap.speak_repeat);

        holder.rmdSubject.setText(agenda.title);
        holder.rmdSubject.setTextColor((active) ? colorOn:colorOff);

        TextView[] tViewWeek = new TextView[7];
        tViewWeek[0] = holder.ltWeek0; tViewWeek[1] = holder.ltWeek1; tViewWeek[2] = holder.ltWeek2;
        tViewWeek[3] = holder.ltWeek3; tViewWeek[4] = holder.ltWeek4; tViewWeek[5] = holder.ltWeek5;
        tViewWeek[6] = holder.ltWeek6;
        String txt = "-";
        holder.tvStartTime.setText(txt);
        holder.tvStartTime.setTextColor((active) ? colorOn:colorOff);
        final SimpleDateFormat sdf = new SimpleDateFormat("MM-DD HH;mm");
        txt = sdf.format(agenda.startTime);
        holder.tvStartTime.setText(txt);
        txt = sdf.format(agenda.finishTime);
        holder.tvFinishTime.setText(txt);
    }

    @Override
    public int getItemCount() {
        return quietTasks.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition != 0 && toPosition != 0) {
            QuietTask quietTask = quietTasks.get(fromPosition);
            quietTasks.remove(quietTask);
            quietTasks.add(toPosition, quietTask);
            notifyItemMoved(fromPosition, toPosition);
            utils.saveQuietTasksToShared();
        } else {
            if (topLine++ < 0)
                Toast.makeText(mContext,"바로 조용히 하기는 맨 위에 있어야... ",Toast.LENGTH_LONG).show();
            else if (topLine > 30)
                topLine = -1;
        }
    }

    @Override
    public void onItemSwiped(int position) {

    }

    public void setTouchHelper(ItemTouchHelper touchHelper){
        this.mTouchHelper = touchHelper;
    }
}