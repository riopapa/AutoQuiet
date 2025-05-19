package better.life.autoquiet.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import better.life.autoquiet.activity.ActivityAddAgenda;
import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.R;
import better.life.autoquiet.Vars;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.models.GCal;
import better.life.autoquiet.Sub.NameColor;
import better.life.autoquiet.Sub.VarsGetPut;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class GCalRecycleAdapter extends RecyclerView.Adapter<GCalRecycleAdapter.ViewHolder> {

    static Vars vars;
    Context context;
    ArrayList<GCal> gCals;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = ContextProvider.get();
        vars = new VarsGetPut().get(context);
//        gCals = new GetAgenda().get(rContext);
        View swipeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gcal_line, parent, false);
        return new ViewHolder(swipeView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View viewLine;
        TextView tvADate, tvADay, tvBegTime, tvEndTime, tvSubject, tvName, tvCalLeft, tvCalRight;
        ImageView ivRepeat;

        public ViewHolder(View itemView) {
            super(itemView);
            this.viewLine = itemView.findViewById(R.id.one_Gcal);
            this.tvADate = itemView.findViewById(R.id.aDate);
            this.tvADay = itemView.findViewById(R.id.aDay);
            this.tvBegTime = itemView.findViewById(R.id.aBegTime);
            this.tvEndTime = itemView.findViewById(R.id.aEndTime);
            this.tvSubject = itemView.findViewById(R.id.calSubject);
            this.tvName = itemView.findViewById(R.id.calName);
            this.ivRepeat = itemView.findViewById(R.id.repeating);
            this.tvCalLeft = itemView.findViewById(R.id.calLeft);
            this.tvCalRight = itemView.findViewById(R.id.calRight);
            this.viewLine.setOnClickListener(v -> {
                int idx = getBindingAdapterPosition();
                Intent intent;
                intent = new Intent(context, ActivityAddAgenda.class);
                intent.putExtra("idx",idx);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }

    final SimpleDateFormat sdfDate = new SimpleDateFormat("dd", Locale.getDefault());
    final SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
    final SimpleDateFormat sdfHHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GCal g = gCals.get(position);
        GCal g0 = null;
        if (position > 0) g0= gCals.get(position-1);
        String s = sdfDate.format(g.begTime);
        if (position> 0 && s.equals(sdfDate.format(g0.begTime))) {
            holder.tvADate.setText("");
            holder.tvADay.setText("");
        }
        else {
            holder.tvADate.setText(sdfDate.format(g.begTime));
            holder.tvADay.setText(sdfDay.format(g.begTime));
        }
        holder.tvSubject.setText(g.title);
        holder.tvName.setText(g.calName);
        holder.ivRepeat.setImageResource((g.repeat)? R.drawable.repeat: null);
        holder.tvCalLeft.setText((g.location.length()>20)?
                g.location.substring(0,19):g.location);
        if (g.desc.length() == 0)
            holder.tvCalRight.setVisibility(View.GONE);
        else
            holder.tvCalRight.setText((g.desc.length()> 20)? g.desc.substring(0,19):g.desc);
        holder.tvBegTime.setText(sdfHHMM.format(g.begTime));
        holder.tvEndTime.setText(sdfHHMM.format(g.endTime));
        int backColor = NameColor.get(g.calName, context);
        holder.viewLine.setBackgroundColor(backColor);
        holder.tvSubject.setSingleLine(true);
        holder.tvSubject.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.tvSubject.setSelected(true);

    }

    @Override
    public int getItemCount() {
        return gCals.size();
    }
}