package com.talhajavedmukhtar.ferret.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.R;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Talha on 11/22/18.
 */

public class HostAdapter extends ArrayAdapter<Host> {
    private String TAG = Tags.makeTag("HostAdapter");
    private ArrayList<Host> hosts;
    private Context context;

    private static class ViewHolder {
        TextView vendorName;
        TextView deviceName;
        TextView ipAddress;
        GifImageView status;
    }

    public HostAdapter(ArrayList<Host> hostData, Context ctx) {
        super(ctx, R.layout.host_item, hostData);
        hosts = hostData;
        context = ctx;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Host host = hosts.get(position);

        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.host_item, parent, false);
            viewHolder.vendorName = (TextView) convertView.findViewById(R.id.vendorName);
            viewHolder.deviceName = convertView.findViewById(R.id.deviceName);
            viewHolder.ipAddress = (TextView) convertView.findViewById(R.id.ipAddress);
            viewHolder.status = (GifImageView) convertView.findViewById(R.id.status);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        String vendor = host.getVendor();

        if (vendor.length() > 14) {
            vendor = vendor.substring(0, 11) + "..";
        }

        viewHolder.vendorName.setText(vendor);
        viewHolder.deviceName.setText(host.getDeviceName());
        viewHolder.ipAddress.setText(host.getIpAddress());

        if (host.getVulnerable() == null) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.Shakespeare));
            viewHolder.status.setImageResource(R.drawable.wait);
        } else if (host.getVulnerable()) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.Blood));
            viewHolder.status.setImageResource(R.drawable.vulnerable);
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.Grass));
            viewHolder.status.setImageResource(R.drawable.safe);
        }

        // Return the completed view to render on screen
        return convertView;
    }


}
