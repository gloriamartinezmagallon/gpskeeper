package navdev.gpstrack.adapter;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import navdev.gpstrack.ActivityDetailsActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.db.Converters;
import navdev.gpstrack.db.Activity;
import navdev.gpstrack.db.ActivityComplete;


public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    List<ActivityComplete> mActivities;
    android.app.Activity mContext;

    public ActivitiesAdapter(android.app.Activity context, List<ActivityComplete> itemList) {
        this.mActivities = itemList;
        this.mContext = context;
    }


    @Override
    public ActivitiesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemrowactivitieslist, parent, false);
        ActivitiesAdapter.ViewHolder viewHolder = new ActivitiesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ActivitiesAdapter.ViewHolder holder, int position) {
        ActivityComplete aux = mActivities.get(position);
        holder.setActivity(aux);
        holder.adddateTV.setText(Converters.dateToString(aux.activity.getAdddate()));
        holder.nameTV.setText(aux.route.get(0).getName());
        holder.infoTV.setText(Converters.distanceToString(aux.activity.getDistance())+"km \n"+Converters.timeToString(aux.activity.getTime()));
    }


    @Override
    public int getItemCount() {
        return mActivities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ActivityComplete mActivity;
        protected TextView adddateTV;
        protected TextView nameTV;
        protected TextView infoTV;

        public void setActivity(ActivityComplete activity){
            this.mActivity = activity;
        }

        public ViewHolder(View view) {
            super(view);
            this.adddateTV = view.findViewById(R.id.adddate);
            this.nameTV = view.findViewById(R.id.route_name);
            this.infoTV = view.findViewById(R.id.activity_info);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newintent = new Intent(mContext, ActivityDetailsActivity.class);
                    newintent.putExtra(ActivityDetailsActivity.ACTIVITY,mActivity);
                    mContext.startActivity(newintent);
                }
            });
        }

    }




}
