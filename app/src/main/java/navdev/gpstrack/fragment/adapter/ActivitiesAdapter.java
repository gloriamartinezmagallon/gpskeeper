package navdev.gpstrack.fragment.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import navdev.gpstrack.MainActivity;
import navdev.gpstrack.R;
import navdev.gpstrack.dao.GpsBBDD;
import navdev.gpstrack.ent.Activity;

/**
 * Created by gloria on 14/08/2015.
 */
public class ActivitiesAdapter extends ArrayAdapter<Activity> {

    List<Activity> items;

    public ActivitiesAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ActivitiesAdapter(Context context, int resource, List<Activity> items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.itemrowactivitieslist, null);
        }

        final Activity p = this.items.get(position);



        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.route_name);
            TextView tt2 = (TextView) v.findViewById(R.id.activity_info);
            TextView tt3 = (TextView) v.findViewById(R.id.adddate);

            String distancia = getContext().getResources().getString(R.string.distancia)+": "+String.format("%.3f",Double.parseDouble(p.getDistance())/1000)+" km";


            String tiempo = getContext().getResources().getString(R.string.tiempo)+": "+((MainActivity) getContext()).timeTostring(Long.parseLong(p.getTime()));

            tt1.setText(p.getRoute(getContext()).getName());
            tt2.setText( distancia+" / "+tiempo);
            tt3.setText(p.getAdddate().toLocaleString());


        }

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.confirmarborraractividad)
                        .setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                GpsBBDD gpsBBDD = new GpsBBDD(getContext());
                                gpsBBDD.deleteActivity(p.getId());

                                items.remove(position);
                                notifyDataSetChanged();
                            }

                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .show();
                return true;
            }
        });

        return v;
    }

}
