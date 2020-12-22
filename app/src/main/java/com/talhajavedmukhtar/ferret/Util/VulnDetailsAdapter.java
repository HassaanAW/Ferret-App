package com.talhajavedmukhtar.ferret.Util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.talhajavedmukhtar.ferret.Model.VulnDetailsData;
import com.talhajavedmukhtar.ferret.R;


public class VulnDetailsAdapter  extends RecyclerView.Adapter<VulnDetailsAdapter.ViewHolder>{

    private VulnDetailsData[] vulndetailsdata;



    // RecyclerView recyclerView;
    public VulnDetailsAdapter(VulnDetailsData[] vulndetailsdata) {
        this.vulndetailsdata = vulndetailsdata;


    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.vulndetails_rowlayout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final VulnDetailsData vulndetail = vulndetailsdata[position];
        holder.ident.setText(vulndetailsdata[position].getidents());
        holder.desc.setText(vulndetailsdata[position].getidentsdescs());

//        holder.imageView.setImageResource(listdata[position].getImgId());
//        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(),"click on item: "+myListData.getDescription(),Toast.LENGTH_LONG).show();
//            }
//        });
    }


    @Override
    public int getItemCount() {
        return vulndetailsdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public ImageView imageView;
        public TextView ident;
        public TextView desc;

        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
//            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.ident = (TextView) itemView.findViewById(R.id.vulnident);
            this.desc = (TextView) itemView.findViewById(R.id.vulndesc);

            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativelayout);
        }
    }


}


