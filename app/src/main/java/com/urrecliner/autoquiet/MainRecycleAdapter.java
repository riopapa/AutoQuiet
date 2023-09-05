package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.ActivityMain.currIdx;
import static com.urrecliner.autoquiet.ActivityMain.mainRecycleAdapter;

import android.content.Context;
import android.content.Intent;
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

import com.urrecliner.autoquiet.Sub.AlarmIcon;
import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.ClearAllTasks;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.MyItemTouchHelperAdapter;
import com.urrecliner.autoquiet.Sub.NameColor;
import com.urrecliner.autoquiet.Sub.VarsGetPut;

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
    private View swipeView;
    Vars vars;
    Context context;

    ArrayList<QuietTask> quietTasks;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        vars = new VarsGetPut().get(ActivityMain.pContext);
        colorOn = ResourcesCompat.getColor(context.getResources(), R.color.colorOn, null);
        colorInactiveBack = ResourcesCompat.getColor(context.getResources(), R.color.colorInactiveBack, null);
        colorOnBack = ResourcesCompat.getColor(context.getResources(), R.color.colorOnBack, null);
        colorOff = ResourcesCompat.getColor(context.getResources(), R.color.colorOff, null);
        colorActive = ResourcesCompat.getColor(context.getResources(), R.color.colorActive, null);
        colorOffBack = ResourcesCompat.getColor(context.getResources(), R.color.colorTransparent, null);

        swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_line, parent, false);

        return new ViewHolder(swipeView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            GestureDetector.OnGestureListener {

        View viewLine;
        ImageView lvVibrate, lvBegLoop, lvEndLoop, lvgCal;
        TextView rmdSubject, rmdDate, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
                tvBegTime, tvEndTime, tvCalRight, tvCalLeft;
        LinearLayout llCalInfo, llBegEndLoop, llBegEndTime, llWeekFlag;
        GestureDetector mGestureDetector;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_reminder);
            this.lvVibrate = itemView.findViewById(R.id.lv_vibrate);
            this.llBegEndLoop = itemView.findViewById(R.id.llBegEnd);
            this.lvBegLoop = itemView.findViewById(R.id.lvBegLoop);
            this.lvEndLoop = itemView.findViewById(R.id.lvEndLoop);
            this.lvgCal = itemView.findViewById(R.id.gCal);
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
        if (!end99) {
            if (qt.vibrate)
                holder.lvVibrate.setImageResource(
                        (active) ? R.drawable.phone_vibrate :R.drawable.transperent);
            else
                holder.lvVibrate.setImageResource(
                        (active) ? R.drawable.phone_off : R.drawable.transperent);
            int begLoop = qt.begLoop;
            int endLoop = qt.endLoop;
            holder.lvBegLoop.setVisibility(View.VISIBLE);
            holder.lvEndLoop.setVisibility(View.VISIBLE);
            holder.lvBegLoop.setImageResource((begLoop == 0) ? R.drawable.speak_off : (begLoop == 1) ? R.drawable.bell_onetime : R.drawable.speak_on);
            holder.lvEndLoop.setImageResource((endLoop == 0) ? R.drawable.speak_off : (endLoop == 1) ? R.drawable.bell_onetime : R.drawable.speak_on);
        } else {
            holder.lvVibrate.setImageResource(new AlarmIcon().getRscId(qt.endHour == 99, qt.vibrate,
                    qt.begLoop, qt.endLoop));
            holder.lvBegLoop.setVisibility(View.GONE);
            holder.lvEndLoop.setVisibility(View.GONE);
        }
        holder.viewLine.setBackgroundColor( ResourcesCompat.getColor(context.getResources(), (position == currIdx) ? R.color.colorSelected: R.color.itemNormalFill, null));

        if (!gCalendar) {
            holder.lvgCal.setImageResource(R.drawable.transperent);
            holder.llCalInfo.setVisibility(View.GONE);
            holder.llBegEndTime.setVisibility(View.VISIBLE);
            holder.llWeekFlag.setVisibility(View.VISIBLE);
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
            holder.lvgCal.setImageResource(R.drawable.calendar);
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

            holder.viewLine.setBackgroundColor(NameColor.get(qt.calName, context));
        }
    }

    String buildHourMin(int hour, int min) { return int2NN(hour)+":"+int2NN(min); }
    String int2NN (int nbr) {
        return (""+(100 + nbr)).substring(1);
    }

    @Override
    public int getItemCount() {
        if (quietTasks == null) {
            context = ActivityMain.pContext;
            quietTasks = new QuietTaskGetPut().get(ActivityMain.pContext);
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
                } else
                    qt.sortKey = i;
            } else
                qt.sortKey = System.currentTimeMillis() + (long) 0x2FFFFFFF + (long) i * 100;
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
            notifyItemRemoved(position);
            new QuietTaskGetPut().put(quietTasks);
//            Snackbar snackbar = Snackbar
//                    .make(swipeView, "다시 살리려면 [복원] 을 누르세요", Snackbar.LENGTH_LONG);
//            snackbar.setAction("복원", view -> {
//                quietTasks.add(position, qt);
//                notifyItemInserted(position);
//                new QuietTaskGetPut().put(quietTasks);
//            });

//            snackbar.setActionTextColor(Color.YELLOW);
//            snackbar.show();

        } else {
            if (topLine++ < 0)
                Toast.makeText(context,"바로 조용히 하기는 삭제 불가능 ... ",Toast.LENGTH_LONG).show();
            else if (topLine > 30)
                topLine = -1;
//           notifyItemChanged(0);
        }
    }

    public void setTouchHelper(ItemTouchHelper tHelper){
        this.mTouchHelper = tHelper;
    }
}