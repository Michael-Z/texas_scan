package com.ruilonglai.texas_scan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;

/**
 * Created by Administrator on 2017/5/18.
 */

public class CardAdapter extends BaseAdapter {
    private int width;
    private int[] colors;
    private Context context;
    private int[] textColors;
    private String[] contents;
    private LayoutInflater inflater;

    public CardAdapter(Context context,String[] contents,int[] colors,int[] textColors,double width){
        this.width = (int)Math.floor(width);
        this.colors = colors;
        this.context = context;
        this.contents = contents;
        this.textColors = textColors;
        inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return contents.length;
    }

    @Override
    public String  getItem(int position) {
        return contents[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if(convertView==null){
            vh = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_card, parent, false);
            ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
            vh.card = (TextView) convertView.findViewById(R.id.card);
            layoutParams.width = width;
            layoutParams.height = width;
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }
        vh.card.setText(contents[position]);
        vh.card.setBackgroundColor(context.getResources().getColor(colors[position]));
        return convertView;
    }
    class ViewHolder{
       public TextView card;
    }
}
