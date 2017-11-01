package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.ShowMes;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/9/12.
 */

public class MesShowAdapter extends RecyclerView.Adapter<MesShowAdapter.ViewHolder> {

    private SparseArray<ShowMes> list;

    private Context context;

    private LayoutInflater inflater;

    private OnItemListener listener;


    public MesShowAdapter(Context context, SparseArray<ShowMes> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemListener(OnItemListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_select, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        if(position%2==0){
//            holder.layout.setBackgroundColor(context.getResources().getColor(R.color.black_overlay));
//        }
        final ShowMes mes = list.valueAt(position);
        holder.percentContent.setText(mes.getPercentContent());
        holder.percentType.setText(mes.getPercentType());
        holder.isSelect.setChecked(mes.getIsSelect()==1?true:false);
        if(listener!=null){
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(position,mes.getIsSelect()==1?false:true);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemListener {

        void onClick(int position, boolean isSelect);

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.percentType)
        TextView percentType;
        @BindView(R.id.percentContent)
        TextView percentContent;
        @BindView(R.id.isSelect)
        CheckBox isSelect;
        @BindView(R.id.layout)
        LinearLayout layout;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
