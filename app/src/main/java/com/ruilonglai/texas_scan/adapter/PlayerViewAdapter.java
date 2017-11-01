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
 * Created by Administrator on 2017/4/14.
 */

public class PlayerViewAdapter extends RecyclerView.Adapter<PlayerViewAdapter.MyViewHolder> {

    private List<PlayerData> players;
    private Context mContext;
    private LayoutInflater inflater;
    private OnItemClickListener mOnItemClickListener;

    //添加一个OnItemClickListener接口，并且定义两个方法
    public interface OnItemClickListener{
        void onClick(int position);
        void onLongClick(int position);
    }

    //然后定义一个监听的方法，便于主类调用
    public void setOnItemListener(OnItemClickListener onItemListener){
        this.mOnItemClickListener = onItemListener;
    }

    public PlayerViewAdapter(Context context, List<PlayerData> players){
        this. mContext=context;
        this. players=players;
        inflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getItemCount() {

        return players.size();
    }

    //填充onCreateViewHolder方法返回的holder中的控件
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.item.setBackgroundColor(mContext.getResources().getColor(R.color.huiBai));
        if(position%2==0){
            holder.item.setBackgroundColor(mContext.getResources().getColor(R.color.hui));
        }
        PlayerData player = players.get(position);
        holder.name.setText(player.getName());
        int playCount = player.getPlayCount();
        holder.totalPlayAndBBCount.setText("("+String.format("%.1f",player.getBbCount())+"bb)");
        holder.winLoseCount.setText("W/L/ALL("+player.getWinCount()+"/"+player.getLoseCount()+"/"+playCount+")");
        if(player.getBbCount()>=0){
            holder.totalPlayAndBBCount.setTextColor(mContext.getResources().getColor(R.color.red));
        }else{
            holder.totalPlayAndBBCount.setTextColor(mContext.getResources().getColor(R.color.green));
        }
        if(playCount!=0){
            if(player.getJoinCount()>0){
                holder.bet3Percent.setText("3Bet("+player.getBet3Count()*100/player.getJoinCount()+"%)");
            }else{
                holder.bet3Percent.setText("3Bet(-%)");
            }
            holder.vpipPercent.setText("VPIP("+player.getJoinCount()*100/playCount+"%)");
            holder.pfrPercent.setText("PFR("+player.getPfrCount()*100/playCount+"%)");
            if(player.getStlPosCount()>0){
                holder.stlPercent.setText("STL("+player.getStlCount()*100/player.getStlPosCount()+"%)");
            }else{
                holder.stlPercent.setText("STL(-%)");
            }
            if(player.getFaceStlCount()>0){
                holder.foldStlPercent.setText("FSTL("+player.getFoldStlCount()*100/player.getFaceStlCount()+"%)");
            }else{
                holder.foldStlPercent.setText("FSTl(-%)");
            }
            if(player.getFace3BetCount()>0){
                holder.fold3BetPercent.setText("F3Bet("+player.getFold3BetCount()*100/player.getFace3BetCount()+"%)");
            }else{
                holder.fold3BetPercent.setText("F3Bet(-%)");
            }
            if(player.getLastRaiseCount()>0){
                holder.cBPercent.setText("CB("+player.getCbCount()*100/player.getLastRaiseCount()+"%)");
            }else{
                holder.cBPercent.setText("CB(-%)");
            }
            if(player.getCallCount()>0){
                double d = player.getRaiseCount()/Double.valueOf(player.getCallCount());
                holder.aFPercent.setText("AF("+String.format("%.1f",d)+")");
            }else{
                holder.aFPercent.setText("AF(-)");
            }
        }
        if(mOnItemClickListener!=null){
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });
            holder.item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return true;
                }
            });
        }
    }

    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_other_player,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout item;
        TextView name;
        TextView totalPlayAndBBCount;
        TextView winLoseCount;
        TextView bet3Percent;
        TextView vpipPercent;
        TextView pfrPercent;
        TextView stlPercent;
        TextView foldStlPercent;
        TextView fold3BetPercent;
        TextView cBPercent;
        TextView aFPercent;

        public MyViewHolder(View view) {
            super(view);
            item = (LinearLayout) view.findViewById(R.id.item);
            name=(TextView) view.findViewById(R.id.playerName);
            totalPlayAndBBCount=(TextView) view.findViewById(R.id.totalPlayAndBBCount);
            winLoseCount=(TextView) view.findViewById(R.id.winLoseCount);
            bet3Percent=(TextView) view.findViewById(R.id.bet3Percent);
            vpipPercent=(TextView) view.findViewById(R.id.vpipPercent);
            pfrPercent=(TextView) view.findViewById(R.id.pfrPercent);
            stlPercent=(TextView) view.findViewById(R.id.stlPercent);
            foldStlPercent=(TextView) view.findViewById(R.id.foldStlPercent);
            fold3BetPercent=(TextView) view.findViewById(R.id.fold3BetPercent);
            cBPercent=(TextView) view.findViewById(R.id.cBPercent);
            aFPercent=(TextView) view.findViewById(R.id.aFPercent);
        }

    }
}