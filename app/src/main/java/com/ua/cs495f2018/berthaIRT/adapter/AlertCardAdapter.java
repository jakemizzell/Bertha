package com.ua.cs495f2018.berthaIRT.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.ua.cs495f2018.berthaIRT.AdminReportDetailsActivity;
import com.ua.cs495f2018.berthaIRT.Client;
import com.ua.cs495f2018.berthaIRT.Message;
import com.ua.cs495f2018.berthaIRT.R;
import com.ua.cs495f2018.berthaIRT.Report;
import com.ua.cs495f2018.berthaIRT.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertCardAdapter extends RecyclerView.Adapter<AlertCardAdapter.AlertViewHolder>{
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    private Context ctx;
    private List<Message> data;
    private TextView tvNoAlerts;

    public AlertCardAdapter(Context c, TextView tvNoAlerts){
        ctx = c;
        data = new ArrayList<>();
        this.tvNoAlerts = tvNoAlerts;
    }

    public void updateAlerts(List<Message> c){
        if(c == null)
            c = new ArrayList<>();

        data.clear();
        data.addAll(c);
        notifyDataSetChanged();

        if(getItemCount() == 0)
            tvNoAlerts.setVisibility(View.VISIBLE);
        else
            tvNoAlerts.setVisibility(View.GONE);
    }

    private void removeAlert(Integer position) {
        //removes the alert for that admin
        Client.net.dismissAlert(ctx, position.toString(), (r)->{
            Integer confirmedPosition = Integer.valueOf(r);
            data.remove((int) confirmedPosition);
            notifyItemRemoved(confirmedPosition);

            if(getItemCount() == 0)
                tvNoAlerts.setVisibility(View.VISIBLE);
            else
                tvNoAlerts.setVisibility(View.GONE);
        });
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlertViewHolder(LayoutInflater.from(ctx).inflate(R.layout.adapter_alertcard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Message a = data.get(position);
        viewBinderHelper.bind(holder.srl, a.getMessageTimestamp().toString()); //for SwipeReveal layout... timestamp is just a unique ID

        //swipe to delete
        holder.srl.setSwipeListener(new SwipeRevealLayout.SimpleSwipeListener() {
            @Override
            public void onOpened(SwipeRevealLayout view){
                removeAlert(holder.getAdapterPosition());
            }
        });

        Report r = Client.reportMap.get(a.getReportID());

        holder.tvAction.setText(a.getMessageBody());
        holder.tvReportID.setText(String.format("%s", a.getReportID()));
        holder.tvTimeSince.setText(calculateTimeSince(a.getMessageTimestamp()));
        holder.tvStatus.setText(r.getStatus());
        //set the text color of status appropriately
        switch (holder.tvStatus.getText().toString()) {
            case "New":
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.NewStatus));
                break;
            case "Open":
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.OpenStatus));
                break;
            case "Assigned":
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.AssignedStatus));
                break;
            case "Closed":
                holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.ClosedStatus));
                break;
        }

        holder.catTainer.removeAllViews();
        //to handle the categories display
        Integer spaceLeft = Client.displayWidthDPI - Util.measureViewWidth(holder.tvStatus);
        spaceLeft -= (8 + 8 + 8 + 8 + 8); // margins
        int hidden = 0;
        for(String cat : r.getCategories()) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.adapter_category, holder.catTainer, false);
            ((TextView) v.findViewById(R.id.adapter_alt_category)).setText(cat);
            int spaceTaken = Util.measureViewWidth(v);
            if(spaceLeft < spaceTaken)
                hidden++;

            else {
                holder.catTainer.addView(v);
                spaceLeft -= spaceTaken;
            }
        }
        if(hidden > 0){
            holder.tvExtraCats.setText(String.format(Locale.US, "+%d", hidden));
            holder.tvExtraCats.setVisibility(View.VISIBLE);
        }

        //if you click on the card the report opens
        holder.cardContainer.setOnClickListener(v -> {
            //get the report clicked on
            Client.activeReport = Client.reportMap.get(a.getReportID());
            ctx.startActivity(new Intent(ctx, AdminReportDetailsActivity.class));
       });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {
        LinearLayout catTainer;
        CardView cardContainer;
        TextView tvTimeSince, tvReportID, tvAction, tvStatus, tvExtraCats;
        SwipeRevealLayout srl;

        AlertViewHolder(View itemView) {
            super(itemView);
            srl = itemView.findViewById(R.id.alertcard_srl);
            catTainer = itemView.findViewById(R.id.alertcard_container_categories);
            cardContainer = itemView.findViewById(R.id.alertcard_cv);
            tvTimeSince = itemView.findViewById(R.id.alertcard_alt_timesince);
            tvReportID = itemView.findViewById(R.id.alertcard_alt_id);
            tvStatus = itemView.findViewById(R.id.alertcard_alt_status);
            tvAction = itemView.findViewById(R.id.alertcard_alt_action);
            tvExtraCats = itemView.findViewById(R.id.alertcard_alt_extracats);
        }
    }

    //Function to display the time since
    private String calculateTimeSince(long last) {
        long diff = System.currentTimeMillis() - last;
        String since;
        //print in seconds
        if(diff < 60000)
            since = (diff/1000) + " SECONDS AGO";
        else if (diff < 3600000)
            since = ((diff/1000) / 60) + " MINUTES AGO";
        else if(diff < 216000000)
            since = (((diff/1000) / 60) / 60) + " HOURS AGO";
        else
            since = ((((diff/1000) / 60) / 60) / 24) + " DAYS AGO";
        return since;
    }
}