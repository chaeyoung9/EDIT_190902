package com.sinc.arshowroom.offline;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sinc.arshowroom.R;

import java.util.List;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.ViewHolder> {

    private List<TestInfo> listItems;

    public TestAdapter(List<TestInfo> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TestInfo listItem = listItems.get(position);

        holder.tv_name.setText(listItem.getSTORE_NAME());
        holder.tv_loc.setText(listItem.getSTORE_LOCATION());
        //holder.tv_lat.setText(String.valueOf(listItem.getSTORE_LATITUDE()));
        //holder.tv_long.setText(String.valueOf(listItem.getSTORE_LONGITUDE()));
        holder.tv_stock.setText(String.valueOf(listItem.getSTOCK_CHECK()));
        holder.tv_dist.setText(String.valueOf(listItem.getSTORE_LOCATION_TO()));

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, listItem.getSTORE_LOCATION_TO() + " 만큼 떨어져 있음", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //사용할 변수들 선언
        private TextView tv_bname;
        private TextView tv_name;
        private TextView tv_loc;
        private TextView tv_stock;
        private TextView tv_dist;
        private CardView cv;


        public ViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.textview1);
            tv_loc = (TextView) itemView.findViewById(R.id.textview2);
            tv_dist=(TextView) itemView.findViewById(R.id.textview3);
            tv_stock = (TextView) itemView.findViewById(R.id.textview4);
            tv_stock.setTextColor(Color.rgb(121,193,66));
            cv = (CardView) itemView.findViewById(R.id.cardView);
        }
    }
}
