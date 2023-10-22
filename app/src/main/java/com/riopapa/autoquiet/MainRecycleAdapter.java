package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.ActivityMain.currIdx;
import static com.riopapa.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.riopapa.autoquiet.ActivityMain.pActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.riopapa.autoquiet.Sub.CalculateNext;
import com.riopapa.autoquiet.Sub.ClearAllTasks;
import com.riopapa.autoquiet.Sub.MyItemTouchHelperAdapter;
import com.riopapa.autoquiet.Sub.NameColor;
import com.riopapa.autoquiet.Sub.VarsGetPut;
import com.riopapa.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class MainRecycleAdapter extends RecyclerView.Adapter<MainRecycleAdapter.ViewHolder>
        implements MyItemTouchHelperAdapter {

    private ItemTouchHelper mTouchHelper;
    private QuietTask qt;
    private int colorOn, colorOnBack, colorInactiveBack, colorOff, colorOffBack, colorActive;
    private int topLine = -1;
    Vars vars;
    Context context;

    ArrayList<QuietTask> quietTasks;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        vars = new VarsGetPut().get(ActivityMain.mContext);
        colorOn = ResourcesCompat.getColor(context.getResources(), R.color.colorOn, null);
        colorInactiveBack = ResourcesCompat.getColor(context.getResources(), R.color.colorInactiveBack, null);
        colorOnBack = ResourcesCompat.getColor(context.getResources(), R.color.colorOnBack, null);
        colorOff = ResourcesCompat.getColor(context.getResources(), R.color.colorOff, null);
        colorActive = ResourcesCompat.getColor(context.getResources(), R.color.colorActive, null);
        colorOffBack = ResourcesCompat.getColor(context.getResources(), R.color.colorTransparent, null);

        View swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_line, parent, false);

        return new ViewHolder(swipeView);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            GestureDetector.OnGestureListener {

        View viewLine;
//        ImageView lvVibrate, lvBegLoop, lvEndLoop, lvgCal;
        ImageView lvAlarmType;
        TextView rmdSubject, rmdDate, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
                tvBegTime, tvEndTime, tvCalRight, tvCalLeft;
        LinearLayout llCalInfo;
        LinearLayout llBegEndTime;
        LinearLayout llWeekFlag;
        GestureDetector mGestureDetector;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_reminder);
            this.lvAlarmType = itemView.findViewById(R.id.lv_alarm_type);
            this.rmdSubject = itemView.findViewById(R.id.rmdSubject);
            this.rmdDate = itemView.findViewById(R.id.rmdDate);
            this.ltWeek0 = itemView.findViewById(R.id.lt_week0);
            this.ltWeek1 = itemView.findViewById(R.id.lt_week1);
            this.ltWeek2 = itemView.findViewById(R.id.lt_week2);
            this.ltWeek3 = itemView.findViewById(R.id.lt_week3);
            this.ltWeek4 = itemView.findViewById(R.id.lt_week4);
            this.ltWeek5 = itemView.findViewById(R.id.lt_week5);
            this.ltWeek6 = itemView.findViewById(R.id.lt_week6);
            this.llBegEndTime = itemView.findViewById(R.id.begEndTime);
            this.tvBegTime = itemView.findViewById(R.id.rmdBegTime);
            this.tvEndTime = itemView.findViewById(R.id.rmdEndTime);
            this.viewLine.setOnClickListener(v -> {
                currIdx = getBindingAdapterPosition();
                qt = quietTasks.get(currIdx);
                Intent intent;
                if (currIdx != 0) {
                    vars.addNewQuiet = false;
                    intent = new Intent(context, ActivityAddEdit.class);
                } else {
                    intent = new Intent(context, ActivityOneTime.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            this.llWeekFlag = itemView.findViewById(R.id.weekFlag);
            this.llCalInfo = itemView.findViewById(R.id.calInfo);
            this.tvCalRight = itemView.findViewById(R.id.calRight);
            this.tvCalLeft = itemView.findViewById(R.id.calLeft);
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
            vars.addNewQuiet = false;
            new VarsGetPut().put(vars, context);
            currIdx = getBindingAdapterPosition();
            Intent intent;
            if (currIdx != 0) {
                intent = new Intent(context, ActivityAddEdit.class);
            } else {
                intent = new Intent(context, ActivityOneTime.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

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

        qt = quietTasks.get(position);

        boolean gCalendar = qt.agenda;
        boolean active = qt.active;

        boolean end99 = qt.endHour == 99;

        holder.rmdSubject.setText(qt.subject);
        holder.rmdSubject.setTextColor((active) ? colorOn : colorOff);

        holder.rmdDate.setVisibility((qt.sayDate)? View.VISIBLE : View.INVISIBLE);
        holder.rmdDate.setTextColor((active) ? colorOn : colorOff);

        String txt = buildHourMin(qt.begHour, qt.begMin);
        holder.tvBegTime.setText(txt);
        txt = (end99) ? "":buildHourMin(qt.endHour, qt.endMin);
        holder.tvEndTime.setText(txt);
        holder.tvBegTime.setTextColor((active) ? colorOn : colorOff);
        holder.tvEndTime.setTextColor((active) ? colorOn : colorOff);

        holder.viewLine.setBackgroundColor( ResourcesCompat.getColor(context.getResources(), (position == currIdx) ? R.color.colorSelected: R.color.itemNormalFill, null));

        if (!gCalendar) {
            holder.lvAlarmType.setImageResource(alarmIcons[qt.alarmType]);
            holder.llCalInfo.setVisibility(View.GONE);
            holder.llBegEndTime.setVisibility(View.VISIBLE);
            holder.llWeekFlag.setVisibility(View.VISIBLE);
            holder.rmdSubject.setBackgroundColor(0x00FFFFFF);
            TextView[] tViewWeek = new TextView[7];
            tViewWeek[0] = holder.ltWeek0;
            tViewWeek[1] = holder.ltWeek1;
            tViewWeek[2] = holder.ltWeek2;
            tViewWeek[3] = holder.ltWeek3;
            tViewWeek[4] = holder.ltWeek4;
            tViewWeek[5] = holder.ltWeek5;
            tViewWeek[6] = holder.ltWeek6;
            if (position == 0) {
                for (int i = 0; i < 7; i++) {
                    tViewWeek[i].setTextColor(colorOffBack);  // transparent
                    tViewWeek[i].setBackgroundColor(colorOffBack);
                }
                txt = "";
                holder.tvBegTime.setText(txt);
            } else {
                boolean[] week = qt.week;
                for (int i = 0; i < 7; i++) {
                    tViewWeek[i].setTextColor(active ? colorActive : colorOff);
                    if (active)
                        tViewWeek[i].setBackgroundColor(week[i] ? colorOnBack : colorOffBack);
                    else
                        tViewWeek[i].setBackgroundColor(week[i] ? colorInactiveBack : colorOffBack);
                }
            }

        } else {
            holder.lvAlarmType.setImageResource(R.drawable.calendar);
            holder.llCalInfo.setVisibility(View.VISIBLE);
            holder.llWeekFlag.setVisibility(View.GONE);
            holder.rmdDate.setVisibility(View.GONE);
            final SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            if (qt.calDesc.equals("") && qt.calLocation.equals("")) {
                holder.tvCalLeft.setText(sdfDate.format(qt.calBegDate));
                holder.tvCalRight.setText("");
            } else if (qt.calLocation.equals("")) {
                holder.tvCalLeft.setText(sdfDate.format(qt.calBegDate));
                holder.tvCalRight.setText(qt.calDesc);
            } else if (qt.calDesc.equals("")) {
                holder.tvCalLeft.setText(qt.calLocation);
                holder.tvCalRight.setText(sdfDate.format(qt.calBegDate));
            } else {
                holder.tvCalLeft.setText(qt.calLocation);
                String s = sdfDate.format(qt.calBegDate) + " ⋙";
                holder.tvCalRight.setText(s);
            }
            holder.tvCalRight.setSingleLine(true);
            holder.tvCalRight.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.tvCalRight.setSelected(true);

            holder.rmdSubject.setBackgroundColor(NameColor.get(qt.calName, context));
        }
    }

    String buildHourMin(int hour, int min) { return int2NN(hour)+":"+int2NN(min); }
    String int2NN (int nbr) {
        return (""+(100 + nbr)).substring(1);
    }

    @Override
    public int getItemCount() {
        if (quietTasks == null) {
            context = ActivityMain.mContext;
            quietTasks = new QuietTaskGetPut().get(ActivityMain.mContext);
            if (quietTasks == null)
                new ClearAllTasks(context);
        }
        return quietTasks.size();
    }

    public void sort() {

        for (int i = 1; i < quietTasks.size(); i++) {
            QuietTask qt = quietTasks.get(i);
            if (qt.active) {
                if (qt.endHour == 99) {
                    qt.sortKey = CalculateNext.calc(false, qt.begHour, qt.begMin, qt.week, 0);
                } else if (qt.agenda) {
                    qt.sortKey = qt.calBegDate;
                } else
                    qt.sortKey = i;
            } else
                qt.sortKey = System.currentTimeMillis() + 9999999999L + (long) i * 1000;

            if (qt.sortKey > 1000) {
                SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd HH:mm:ss ", Locale.getDefault());
                Log.w("seq "+i, sdfDate.format(qt.sortKey) +" "+qt.subject);
            }

            quietTasks.set(i, qt);
        }
        quietTasks.sort(Comparator.comparingLong(arg0 -> arg0.sortKey));
        mainRecycleAdapter.notifyDataSetChanged();
        new QuietTaskGetPut().put(quietTasks);
        Toast.makeText(context, "Sorted by next Time", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition != 0 && toPosition != 0) {
            QuietTask quietTask = quietTasks.get(fromPosition);
            quietTasks.remove(quietTask);
            quietTasks.add(toPosition, quietTask);
            notifyItemMoved(fromPosition, toPosition);
            new QuietTaskGetPut().put(quietTasks);
        } else {
            if (topLine++ < 0)
                Toast.makeText(context,"바로 조용히 하기는 맨 위에 있어야... ",Toast.LENGTH_LONG).show();
            else if (topLine > 30)
                topLine = -1;
        }
    }

    @Override
    public void onItemSwiped(int position) {
        if (position != 0) {
            qt = quietTasks.get(position);
            quietTasks.remove(position);
            mainRecycleAdapter.notifyItemRemoved(position);
            new QuietTaskGetPut().put(quietTasks);
            View pView = pActivity.findViewById(R.id.mainRecycler);
            Snackbar snackbar = Snackbar
                    .make(pView, "다시 살리려면 [복원] 을 누르세요", Snackbar.LENGTH_LONG);
            snackbar.setAction("복원", view -> {
                quietTasks.add(position, qt);
                new QuietTaskGetPut().put(quietTasks);
                mainRecycleAdapter.notifyDataSetChanged();
            });

            snackbar.setActionTextColor(Color.CYAN);
            snackbar.show();

        } else {
            if (topLine++ < 0)
                Toast.makeText(context,"바로 조용히 하기는 삭제 불가능 ... ",Toast.LENGTH_LONG).show();
            else if (topLine > 30)
                topLine = -1;
        }
    }

    public void setTouchHelper(ItemTouchHelper tHelper){
        this.mTouchHelper = tHelper;
    }
}