package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;

/**
 * Created by wangshaui on 2017/5/16.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppHolder> {
    Context context;
    String[] list;
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
    public AppAdapter(Context context, String[] list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_app, parent,false);
        AppHolder vh = new AppHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(AppHolder holder, final int position) {
        holder.appName.setText(list[position]);
        if("德扑圈".equals(list[position])){
            holder.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpq));
            holder.appName.setTextColor(context.getResources().getColor(R.color.red));
        }else if("德友圈".equals(list[position])){
            holder.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dyq));
        }else if("扑克部落MTT".equals(list[position])){
            holder.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpbl));
        }else if("扑克部落SNG".equals(list[position])){
            holder.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpbl));
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
        return list.length;
    }

    class AppHolder extends RecyclerView.ViewHolder{
        TextView appName;
        ImageView appIcon;
        LinearLayout layout;
        public AppHolder(View itemView) {
            super(itemView);
            appName = (TextView) itemView.findViewById(R.id.appName);
            appIcon = (ImageView) itemView.findViewById(R.id.appIcon);
            layout = (LinearLayout) itemView.findViewById(R.id.item_layout);
        }
    }
}
