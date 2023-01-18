package com.urrecliner.autoquiet;

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
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.ItemTouchHelperAdapter;
import com.urrecliner.autoquiet.utility.NameColor;
import com.urrecliner.autoquiet.utility.VarsGetPut;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainRecycleAdapter extends RecyclerView.Adapter<MainRecycleAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private ItemTouchHelper mTouchHelper;
    private QuietTask quietTask;
    private int colorOn, colorOnBack, colorInactiveBack, colorOff, colorOffBack, colorActive;
    private int topLine = -1;
    private View swipeView;
    Vars vars;
    Context context;

    ArrayList<QuietTask> quietTasks;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        vars = new VarsGetPut().get(MainActivity.pContext);
        colorOn = ResourcesCompat.getColor(context.getResources(), R.color.colorOn, null);
        colorInactiveBack = ResourcesCompat.getColor(context.getResources(), R.color.colorInactiveBack, null);
        colorOnBack = ResourcesCompat.getColor(context.getResources(), R.color.colorOnBack, null);
        colorOff = ResourcesCompat.getColor(context.getResources(), R.color.colorOff, null);
        colorActive = ResourcesCompat.getColor(context.getResources(), R.color.colorActive, null);
        colorOffBack = ResourcesCompat.getColor(context.getResources(), R.color.colorTransparent, null);

        swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_line, parent, false);

        return new ViewHolder(swipeView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            GestureDetector.OnGestureListener
    {

        View viewLine;
        ImageView lvVibrate, lvStartRepeat, lvFinishRepeat, lvgCal;
        TextView rmdSubject, ltWeek0, ltWeek1, ltWeek2, ltWeek3, ltWeek4, ltWeek5, ltWeek6,
                tvStartTime, tvFinishTime, tvCalRight, tvCalLeft;
        LinearLayout llCalInfo, llStartFinishTime, llWeekFlag;
        GestureDetector mGestureDetector;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_reminder);
            this.lvVibrate = itemView.findViewById(R.id.lv_vibrate);
            this.lvStartRepeat = itemView.findViewById(R.id.lv_startRepeat);
            this.lvFinishRepeat = itemView.findViewById(R.id.lv_finishRepeat);
            this.lvgCal = itemView.findViewById(R.id.gCal);
            this.rmdSubject = itemView.findViewById(R.id.rmdSubject);
            this.ltWeek0 = itemView.findViewById(R.id.lt_week0);
            this.ltWeek1 = itemView.findViewById(R.id.lt_week1);
            this.ltWeek2 = itemView.findViewById(R.id.lt_week2);
            this.ltWeek3 = itemView.findViewById(R.id.lt_week3);
            this.ltWeek4 = itemView.findViewById(R.id.lt_week4);
            this.ltWeek5 = itemView.findViewById(R.id.lt_week5);
            this.ltWeek6 = itemView.findViewById(R.id.lt_week6);
            this.llStartFinishTime = itemView.findViewById(R.id.startFinishTime);
            this.tvStartTime = itemView.findViewById(R.id.rmdStartTime);
            this.tvFinishTime = itemView.findViewById(R.id.rmdFinishTime);
            this.viewLine.setOnClickListener(v -> {
                int qIdx = getBindingAdapterPosition();
                quietTask = quietTasks.get(qIdx);
                Intent intent;
                if (qIdx != 0) {
                    vars.addNewQuiet = false;
                    intent = new Intent(context, AddUpdateActivity.class);
                } else {
                    intent = new Intent(context, OneTimeActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                Gson gson = new Gson();
//                String json = gson.toJson(vars);
//                sharedEditor.putString("MyObject", json);

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
            int qIdx = getBindingAdapterPosition();
            if (qIdx >= quietTasks.size())
                return true;
            quietTask = quietTasks.get(qIdx);
            Intent intent;
            if (qIdx != 0) {
                vars.addNewQuiet = false;
                intent = new Intent(context, AddUpdateActivity.class);
            } else {
                intent = new Intent(context, OneTimeActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("idx",qIdx);
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

        quietTask = quietTasks.get(position);
        boolean gCalendar = quietTask.agenda;
        boolean active = quietTask.active;
        boolean vibrate = quietTask.vibrate;

        holder.rmdSubject.setText(quietTask.subject);
        holder.rmdSubject.setTextColor((active) ? colorOn : colorOff);

        String txt = buildHourMin(quietTask.startHour, quietTask.startMin);
        holder.tvStartTime.setText(txt);
        txt = buildHourMin(quietTask.finishHour, quietTask.finishMin);
        holder.tvFinishTime.setText(txt);
        holder.tvStartTime.setTextColor((active) ? colorOn : colorOff);
        holder.tvFinishTime.setTextColor((active) ? colorOn : colorOff);
        if (vibrate)
            holder.lvVibrate.setImageResource((active) ? R.drawable.phone_vibrate : R.mipmap.speaking_noactive);
        else
            holder.lvVibrate.setImageResource((active) ? R.drawable.phone_vibrate : R.mipmap.speaking_noactive);
        int sRepeat = quietTask.sRepeatCount;
        int fRepeat = quietTask.fRepeatCount;
        holder.lvStartRepeat.setImageResource((sRepeat == 0) ? R.drawable.speak_off : (sRepeat == 1) ? R.drawable.speak_on : R.mipmap.speak_repeat);
        holder.lvFinishRepeat.setImageResource((fRepeat == 0) ? R.drawable.speak_off : (fRepeat == 1) ? R.drawable.speak_on : R.mipmap.speak_repeat);
        holder.viewLine.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.itemNormalFill, null));

        if (!gCalendar) {
            holder.lvgCal.setImageResource(R.mipmap.speaking_noactive);
            holder.llCalInfo.setVisibility(View.GONE);
            holder.llStartFinishTime.setVisibility(View.VISIBLE);
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
                holder.tvStartTime.setText(txt);
                holder.tvFinishTime.setText(txt);
            } else {
                boolean[] week = quietTask.week;
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
            final SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd(EEE)", Locale.getDefault());
            if (quietTask.calDesc.equals("") && quietTask.calLocation.equals("")) {
                holder.tvCalLeft.setText(sdfDate.format(quietTask.calStartDate));
                holder.tvCalRight.setText("");
            } else if (quietTask.calLocation.equals("")) {
                holder.tvCalLeft.setText(sdfDate.format(quietTask.calStartDate));
                holder.tvCalRight.setText(quietTask.calDesc);
            } else if (quietTask.calDesc.equals("")) {
                holder.tvCalLeft.setText(quietTask.calLocation);
                holder.tvCalRight.setText(sdfDate.format(quietTask.calStartDate));
            } else {
                holder.tvCalLeft.setText(quietTask.calLocation);
                String s = sdfDate.format(quietTask.calStartDate) + " ⋙";
                holder.tvCalRight.setText(s);
            }
//            holder.tvCalLeft.setTextColor(colorOn);
//            holder.tvCalRight.setTextColor(colorOn);
            holder.tvCalRight.setSingleLine(true);
            holder.tvCalRight.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.tvCalRight.setSelected(true);

            holder.viewLine.setBackgroundColor(NameColor.get(quietTask.calName, context));
        }
    }

    String buildHourMin(int hour, int min) { return int2NN(hour)+":"+int2NN(min); }
    String int2NN (int nbr) {
        return (""+(100 + nbr)).substring(1);
    }

    @Override
    public int getItemCount() {
        if (quietTasks == null) {
            context = MainActivity.pContext;
            quietTasks = new QuietTaskGetPut().get(MainActivity.pContext);
        }
        return quietTasks.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition != 0 && toPosition != 0) {
            QuietTask quietTask = quietTasks.get(fromPosition);
            quietTasks.remove(quietTask);
            quietTasks.add(toPosition, quietTask);
            notifyItemMoved(fromPosition, toPosition);
            new QuietTaskGetPut().put(quietTasks, context, "Moved "+quietTask.subject);
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
            quietTask = quietTasks.get(position);
            quietTasks.remove(position);
            notifyItemRemoved(position);
            new QuietTaskGetPut().put(quietTasks, context, "Swipe "+quietTask.subject);
            Snackbar snackbar = Snackbar
                    .make(swipeView, "다시 살리려면 [복원] 을 누르세요", Snackbar.LENGTH_LONG);
            snackbar.setAction("복원", view -> {
                quietTasks.add(position, quietTask);
                notifyItemInserted(position);
                new QuietTaskGetPut().put(quietTasks, context, "recover "+quietTask.subject);
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

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