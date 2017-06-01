package navdev.gpstrack.fragment.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import navdev.gpstrack.R;
import navdev.gpstrack.ent.Route;

/**
 * Created by gloria on 14/08/2015.
 */
public class RoutesAdapter extends ArrayAdapter<Route> {

    public RoutesAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RoutesAdapter(Context context, int resource, List<Route> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.itemrowrouteslist, null);
        }

        Route p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.route_name);

            if (tt1 != null) {
                tt1.setText(p.getName());
            }
        }

        return v;
    }

}
