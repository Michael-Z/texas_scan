package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.PlayerData;

import java.util.List;

/**
 * Created by wangshuai on 2017/5/9.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private List<PlayerData> list;
    private Context context;
    private LayoutInflater inflater;
    public MyAdapter(Context context, List<PlayerData> list){
       this.list = list;
       this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(position%2!=0){
            holder.item.setBackgroundColor(context.getResources().getColor(R.color.hui));
        }
        PlayerData playerData = list.get(position);
        holder.seatFlag.setText(playerData.getSeatFlag());
        holder.playHand.setText(playerData.getPlayCount()+"");
        if(playerData.getPlayCount()>0){
            holder.winPercent.setText(Math.floor(playerData.getWinCount()*100/playerData.getPlayCount())+"%");
        }else{
            holder.winPercent.setText("0.00%");
        }
        double money = Math.floor(playerData.getBbCount() * 100 / playerData.getPlayCount()) / 100;
        if(Double.isNaN(money)){
            money = 0;
        }
        holder.winMoney.setText(money +"bb");
        if(money>=0){
            holder.winMoney.setTextColor(context.getResources().getColor(R.color.red));
        }else{
            holder.winMoney.setTextColor(context.getResources().getColor(R.color.green));
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_self_data,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView seatFlag;
        TextView playHand;
        TextView winPercent;
        TextView winMoney;
        LinearLayout item;

        public MyViewHolder(View view) {
            super(view);
            item = (LinearLayout) view.findViewById(R.id.item);
            seatFlag=(TextView) view.findViewById(R.id.name);
            winMoney=(TextView) view.findViewById(R.id.abc);
            winPercent=(TextView) view.findViewById(R.id.winCount);
            playHand=(TextView) view.findViewById(R.id.winMoney);
        }

    }
}
