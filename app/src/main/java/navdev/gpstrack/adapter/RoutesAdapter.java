package navdev.gpstrack.adapter;


import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import navdev.gpstrack.R;
import navdev.gpstrack.RoutedetailsActivity;
import navdev.gpstrack.db.Route;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {

    List<Route> mRoutes;
    Activity mContext;

    public RoutesAdapter(Activity context, List<Route> itemList) {
        this.mRoutes = itemList;
        this.mContext = context;
    }


    @Override
    public RoutesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemrowrouteslist, parent, false);
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
            this.textView = (TextView) view.findViewById(R.id.route_name);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newintent = new Intent(mContext, RoutedetailsActivity.class);
                    newintent.putExtra(RoutedetailsActivity.ROUTE,mRoute);
                    mContext.startActivity(newintent);
                }
            });
        }

    }
}
