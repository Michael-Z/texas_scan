package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;

import java.util.List;

/**
 * Created by wangshaui on 2017/5/16.
 */

public class HandLogAdapter extends RecyclerView.Adapter<HandLogAdapter.HandLogViewHolder> {
    Context context;
    List<String> list;
    LayoutInflater inflater;
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
    public HandLogAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public HandLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_log, parent,false);
        HandLogViewHolder vh = new HandLogViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(HandLogViewHolder holder, final int position) {
         holder.log.setText(list.get(position));
        if(position%2==0){
            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.hui));
        }
        if(mOnItemClickListener!=null){
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });
            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onLongClick(position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HandLogViewHolder extends RecyclerView.ViewHolder{
        TextView log;
        LinearLayout layout;
        public HandLogViewHolder(View itemView) {
            super(itemView);
            log = (TextView) itemView.findViewById(R.id.log_item);
            layout = (LinearLayout) itemView.findViewById(R.id.item_layout);
        }
    }
}
