package com.urrecliner.autoquiet;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.urrecliner.autoquiet.models.GCal;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.ItemTouchHelperAdapter;
import com.urrecliner.autoquiet.utility.NameColor;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.addNewQuiet;
import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

public class GCalRecycleViewAdapter extends RecyclerView.Adapter<GCalRecycleViewAdapter.ViewHolder> {

    private ItemTouchHelper mTouchHelper;
    private View swipeView;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gcal_line, parent, false);
        return new ViewHolder(swipeView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            GestureDetector.OnGestureListener
    {

        View viewLine;
        TextView tvADate, tvADay, tvSTime, tvFTime, tvSubject, tvName, tvLoc, tvDesc;
        ImageView ivRepeat;
        GestureDetector mGestureDetector;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_Gcal);
            this.tvADate = itemView.findViewById(R.id.aDate);
            this.tvADay = itemView.findViewById(R.id.aDay);
            this.tvSTime = itemView.findViewById(R.id.aSTime);
            this.tvFTime = itemView.findViewById(R.id.aFTime);
            this.tvSubject = itemView.findViewById(R.id.calSubject);
            this.tvName = itemView.findViewById(R.id.calName);
            this.ivRepeat = itemView.findViewById(R.id.repeating);
            this.tvLoc = itemView.findViewById(R.id.calLoc);
            this.tvDesc = itemView.findViewById(R.id.calDesc);
            this.viewLine.setOnLongClickListener(v -> {
//                int qIdx = getBindingAdapterPosition();
//                GCal = GCals.get(qIdx);
//                Intent intent;
//                if (qIdx != 0) {
//                    addNewQuiet = false;
//                    intent = new Intent(mContext, AddUpdateActivity.class);
//                } else {
//                    intent = new Intent(mContext, OneTimeActivity.class);
//                }
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intent);
                return false;
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
            GCal gCal = gCals.get(qIdx);
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

    final SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());
    final SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
    final SimpleDateFormat sdfHHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm ");

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GCal g = gCals.get(position);
        GCal g0 = null;
        if (position > 0) g0= gCals.get(position-1);
        String s = sdfDate.format(g.startTime);
        if (position> 0 && s.equals(sdfDate.format(g0.startTime))) {
            holder.tvADate.setText("");
            holder.tvADay.setText("");
        }
        else {
            holder.tvADate.setText(sdfDate.format(g.startTime));
            holder.tvADay.setText(sdfDay.format(g.startTime));
        }
        holder.tvSubject.setText(g.title);
        holder.tvName.setText(g.calName);
        holder.ivRepeat.setImageResource((g.repeat)? R.mipmap.repeating: R.mipmap.transparent);
        holder.tvLoc.setText((g.location.length()>20)? g.location.substring(0,19):g.location);
        if (g.desc.length() == 0)
            holder.tvDesc.setVisibility(View.GONE);
        else
            holder.tvDesc.setText((g.desc.length()> 20)? g.desc.substring(0,19):g.desc);
        holder.tvSTime.setText(sdfHHMM.format(g.startTime));
        holder.tvFTime.setText(sdfHHMM.format(g.finishTime));
        int backColor = NameColor.get(g.calName);
        holder.viewLine.setBackgroundColor(backColor);
    }

    @Override
    public int getItemCount() {
        return gCals.size();
    }
}
