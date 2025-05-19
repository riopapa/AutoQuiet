package better.life.autoquiet.adapter;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_ONETIME;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_WEEKLY;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.activity.ActivityMain.currIdx;
import static better.life.autoquiet.activity.ActivityMain.mainRecycleAdapter;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
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
import better.life.autoquiet.activity.ActivityAddEdit;
import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.activity.ActivityOneTime;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.R;
import better.life.autoquiet.calendar.CalcNextBegEnd;
import better.life.autoquiet.quiettask.QuietTaskNew;
import better.life.autoquiet.Sub.MyItemTouchHelperAdapter;
import better.life.autoquiet.Sub.NameColor;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.Vars;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
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
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        vars = new VarsGetPut().get(context);
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
        TextView rmdSubject, rmdDate, rmdVibrate, rmdClock, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
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
            this.rmdVibrate = itemView.findViewById(R.id.rmdVibrate);
            this.rmdClock = itemView.findViewById(R.id.rmdClock);
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

        holder.rmdVibrate.setVisibility((qt.vibrate)? View.VISIBLE : View.INVISIBLE);
        holder.rmdVibrate.setTextColor((active) ? colorOn : colorOff);

        holder.rmdClock.setVisibility((qt.clock)? View.VISIBLE : View.INVISIBLE);
        holder.rmdClock.setTextColor((active) ? colorOn : colorOff);

        String txt = buildHourMin(qt.begHour, qt.begMin);
        holder.tvBegTime.setText(txt);
        txt = (end99) ? "":buildHourMin(qt.endHour, qt.endMin);
        holder.tvEndTime.setText(txt);
        holder.tvBegTime.setTextColor((active) ? colorOn : colorOff);
        holder.tvEndTime.setTextColor((active) ? colorOn : colorOff);

        holder.viewLine.setBackgroundColor(ResourcesCompat.getColor(context.getResources(),
                (position == currIdx) ? R.color.colorSelected: R.color.itemNormalFill, null));

        if (!gCalendar) {
            holder.lvAlarmType.setImageResource(alarmIcons[qt.alarmType]);
            holder.lvAlarmType.setAlpha((active)? 1f: 0.6f);
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
            if (qt.calDesc.isEmpty() && qt.calLocation.isEmpty()) {
                holder.tvCalLeft.setText(sdfDate.format(qt.calBegDate));
                holder.tvCalRight.setText("");
            } else if (qt.calLocation.isEmpty()) {
                holder.tvCalLeft.setText(sdfDate.format(qt.calBegDate));
                holder.tvCalRight.setText(qt.calDesc);
            } else if (qt.calDesc.isEmpty()) {
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
        return (String.valueOf(100 + nbr)).substring(1);
    }

    @Override
    public int getItemCount() {
        if (quietTasks == null) {
            context = ContextProvider.get();
            quietTasks = new QuietTaskGetPut().get();
            if (quietTasks == null)
                new QuietTaskNew();
        }
        return quietTasks.size();
    }

    public void sort() {

        if (quietTasks == null || quietTasks.isEmpty())
            return;
        // generate sort key
        for (int i = 1; i < quietTasks.size(); i++) {   // start 1 except 0 : 바로 조용히
            QuietTask qt = quietTasks.get(i);
            if (qt.alarmType == BELL_SEVERAL || qt.alarmType == BELL_ONETIME || qt.alarmType == BELL_WEEKLY) {
                qt.sortKey = 90000000L + qt.begHour * 100L + qt.begMin;
            } else if (qt.active) {
                if (qt.endHour == 99) {
                    CalcNextBegEnd cal = new CalcNextBegEnd(qt);
                    qt.sortKey = cal.begTime;
                } else if (qt.agenda) {
                    qt.sortKey = qt.calBegDate;
                } else
                    qt.sortKey = (long) i * 10;
            } else if (qt.alarmType < PHONE_VIBRATE) {  // alert, oneTime, but inactive ...
                qt.sortKey = System.currentTimeMillis() + 9999999999L + qt.begHour * 100L + qt.begMin;
            } else {    // beg + end normal
                qt.sortKey = 10000 + (long) i * 10;
            }

            quietTasks.set(i, qt);
        }
        quietTasks.sort(Comparator.comparingLong(arg0 -> arg0.sortKey));
        mainRecycleAdapter.notifyDataSetChanged();
        new QuietTaskGetPut().put(quietTasks);
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
            View pView = ((Activity) ContextProvider.get()).findViewById(R.id.mainRecycler);
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