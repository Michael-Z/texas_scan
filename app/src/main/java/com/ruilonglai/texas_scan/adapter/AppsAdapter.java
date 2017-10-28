package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.entity.PokerBest;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/10/26.
 */

public class AppsAdapter extends BaseAdapter {

    private Context context;

    private String[] list;

    private SparseArray<PokerBest> hands;

    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(int position,int id);
    }

    public AppsAdapter(Context context, String[] apps, SparseArray<PokerBest> hands) {
        this.context = context;
        this.list = apps;
        this.hands = hands;
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public String getItem(int position) {
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView==null){
            vh = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_perform, parent, false);
            vh.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            vh.appName = (TextView) convertView.findViewById(R.id.appName);
            vh.layout_app = (LinearLayout) convertView.findViewById(R.id.layout_app);
            vh.layoutPokerDetails = (LinearLayout) convertView.findViewById(R.id.layout_poker_details);
            vh.hand = (TextView) convertView.findViewById(R.id.hand);
            vh.position = (TextView) convertView.findViewById(R.id.position);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }
        vh.appName.setText(list[position]);
        if("德扑圈".equals(list[position])){
            vh.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpq));
        }else if("德友圈".equals(list[position])){
            vh.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dyq));
        }else if("扑克部落MTT".equals(list[position])){
            vh.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpbl));
        }else if("扑克部落SNG".equals(list[position])){
            vh.appIcon.setBackground(context.getResources().getDrawable(R.drawable.icon_dpbl));
        }
        PokerBest pokerBest = hands.get(position);
        if(pokerBest ==null){
            vh.layoutPokerDetails.setVisibility(View.GONE);
            vh.hand.setText("");
            vh.position.setText("");
            if(position==0){
                vh.layoutPokerDetails.setVisibility(View.VISIBLE);
                vh.hand.setText("23e2qwqw");
                vh.position.setText("qwrqwerqw");
            }
        }else{
            vh.layoutPokerDetails.setVisibility(View.VISIBLE);
            vh.hand.setText("");
            vh.position.setText("");
        }
        vh.layout_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onClick(position,0);
                }
            }
        });
        vh.layoutPokerDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onClick(position,1);
                }
            }
        });
        return convertView;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    class ViewHolder {

        ImageView appIcon;

        TextView appName;

        TextView hand;

        TextView position;

        LinearLayout layoutPokerDetails;

        LinearLayout layout_app;
    }
}
