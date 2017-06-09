package navdev.gpstrack.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import navdev.gpstrack.ImportRouteActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.RoutedetailsActivity;
import navdev.gpstrack.ent.Route;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    ArrayList<Route> mRoutes;
    Activity mContext;

    public RoutesAdapter(Activity context, ArrayList<Route> itemList) {
        this.mRoutes = itemList;
        this.mContext = context;
    }


    @Override
    public RoutesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        RoutesAdapter.ViewHolder viewHolder = new RoutesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setRoute(mRoutes.get(position));
        holder.textView.setText(mRoutes.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return mRoutes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        Route mRoute;
        protected TextView textView;

        public void setRoute(Route route){
            this.mRoute = route;
        }

        public ViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(android.R.id.text1);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newintent = new Intent(mContext, RoutedetailsActivity.class);
                    newintent.putExtra(RoutedetailsActivity.ID_ROUTE,mRoute.getId());
                    mContext.startActivity(newintent);
                    mContext.finish();
                }
            });
        }

    }
}
