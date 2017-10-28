package com.ruilonglai.texas_scan.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.AppsAdapter;
import com.ruilonglai.texas_scan.entity.PokerBest;
import com.ruilonglai.texas_scan.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

/**
 * 　　　　　　　　┏┓　　　┏┓
 * 　　　　　　　┏┛┻━━━┛┻┓
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃
 * 　　　　　　 ████━████     ┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　 　 ┗━━━┓
 * 　　　　　　　　　┃ 神兽保佑　　 ┣┓
 * 　　　　　　　　　┃ 代码无BUG   ┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛
 * Created by 王健(Jarek) on 2016/9/9.
 */
public class HomeFragment extends Fragment {

    private ViewHolder vh;
    private MainActivity context;
    private SparseArray<PokerBest> hands;
    private boolean haveCreateOpenView;
    private int lastPostion;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        vh = new ViewHolder(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (MainActivity) context;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }
    public void initView(){
        hands = new SparseArray<>();
        vh.phone.setText(context.getSharedPreferences(LoginActivity.PREF_FILE, MODE_PRIVATE).getString("name", ""));
        vh.title.setText("欢迎使用德扑数据大师");
        final AppsAdapter adapter = new AppsAdapter(context, Constant.APPNAMES,hands);
        adapter.setOnItemClickListener(new AppsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, int id) {
                if(id==0){
                    context.setTemplate(position+8);
                    int count = vh.performList.getChildCount();
                    TextView item = (TextView) vh.performList.getChildAt(position).findViewById(R.id.appName);
                    TextView lastItem = (TextView) vh.performList.getChildAt(lastPostion).findViewById(R.id.appName);
                    if(lastPostion==position){
                         if(!haveCreateOpenView){
                             haveCreateOpenView =  context.wt.createOpenView(context);
                             item.setTextColor(context.getResources().getColor(R.color.red));
                             Toast.makeText(context,Constant.APPNAMES[position], Toast.LENGTH_SHORT).show();
                         }else{
                             context.wt.deleteWindow(true);
                             item.setTextColor(context.getResources().getColor(R.color.black_overlay));
                             haveCreateOpenView = false;
                         }
                    }else{
                        item.setTextColor(context.getResources().getColor(R.color.red));
                        lastItem.setTextColor(context.getResources().getColor(R.color.black_overlay));
                        if(!haveCreateOpenView){
                            haveCreateOpenView =  context.wt.createOpenView(context);
                            Toast.makeText(context,Constant.APPNAMES[position], Toast.LENGTH_SHORT).show();
                        }
                        lastPostion = position;
                    }
                }else{
                    //跳转到另外一个activity
                }

            }
        });
        vh.performList.setAdapter(adapter);

    }
    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.head)
        ImageView head;
        @BindView(R.id.phone)
        TextView phone;
        @BindView(R.id.have_login)
        TextView haveLogin;
        @BindView(R.id.remain_login)
        TextView remainLogin;
        @BindView(R.id.serical_num)
        TextView sericalNum;
        @BindView(R.id.best_hand)
        TextView bestHand;
        @BindView(R.id.bad_hand)
        TextView badHand;
        @BindView(R.id.performList)
        GridView performList;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
