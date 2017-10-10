package navdev.gpstrack.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import navdev.gpstrack.ActivityDetailsActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.RoutedetailsActivity;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Activity;
import navdev.gpstrack.ent.Route;


public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    ArrayList<Activity> mActivities;
    android.app.Activity mContext;

    public ActivitiesAdapter(android.app.Activity context, ArrayList<Activity> itemList) {
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
        Activity aux = mActivities.get(position);
        holder.setActivity(aux);
        holder.adddateTV.setText(aux.getAdddatetoformat());
        holder.nameTV.setText(aux.getRoute(mContext).getName());
        holder.infoTV.setText(aux.getDistanceKm()+"\n"+aux.timeTostring());
    }


    @Override
    public int getItemCount() {
        return mActivities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        Activity mActivity;
        protected TextView adddateTV;
        protected TextView nameTV;
        protected TextView infoTV;

        public void setActivity(Activity activity){
            this.mActivity = activity;
        }

        public ViewHolder(View view) {
            super(view);
            this.adddateTV = (TextView) view.findViewById(R.id.adddate);
            this.nameTV = (TextView) view.findViewById(R.id.route_name);
            this.infoTV = (TextView) view.findViewById(R.id.activity_info);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newintent = new Intent(mContext, ActivityDetailsActivity.class);
                    newintent.putExtra(ActivityDetailsActivity.ID_ACTIVITY,mActivity.getId());
                    mContext.startActivity(newintent);
                }
            });
        }

    }




}
