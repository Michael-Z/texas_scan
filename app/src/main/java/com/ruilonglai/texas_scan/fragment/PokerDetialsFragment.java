package com.ruilonglai.texas_scan.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruilonglai.texas_scan.MainActivity;
import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.activity.LoginActivity;
import com.ruilonglai.texas_scan.adapter.CardAdapter;
import com.ruilonglai.texas_scan.data.MyDataUtil;
import com.ruilonglai.texas_scan.entity.MyData;
import com.ruilonglai.texas_scan.entity.QueryPokerData;
import com.ruilonglai.texas_scan.entity.ReqData;
import com.ruilonglai.texas_scan.entity.Result;
import com.ruilonglai.texas_scan.util.ActionsTool;
import com.ruilonglai.texas_scan.util.CardUtil;
import com.ruilonglai.texas_scan.util.Constant;
import com.ruilonglai.texas_scan.util.GsonUtil;
import com.ruilonglai.texas_scan.util.HttpUtil;
import com.ruilonglai.texas_scan.util.TimeUtil;

import org.angmarch.views.NiceSpinner;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/10/26.
 */

public class PokerDetialsFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, AdapterView.OnItemClickListener {
    @BindColor(R.color.nocolor)
    int noColor;
    private ViewHolder vh;
    private MainActivity context;
    private List<MyData> cardDataList;
    private CardAdapter cardAdapter;
    private int[] colors = new int[169];
    private int[] textColors = new int[169];
    private String[] contents = new String[169];
    private DatePickerDialog datePickerDialog = null;
    public static final String DATEPICKER_TAG = "datepicker";
    String[] cards = {"A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2"};
    List<String> strings = Arrays.asList("今天", "全部", "3天", "一周", "15天", "一个月", "三个月");
    private int[] eightColors = {R.color.hui, R.color.win_4, R.color.win_3, R.color.win_2, R.color.win_1, R.color.los_1, R.color.los_2, R.color.los_3, R.color.los_4};
    private int[] eightTextColors = {android.R.color.black, android.R.color.white, android.R.color.white, android.R.color.black, android.R.color.black, android.R.color.black, android.R.color.black, android.R.color.black, android.R.color.white};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_poker_detail, null, false);
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
        getCardsInData(new Date());
    }

    private void initView() {
        vh.title.setText("手牌详情");
        vh.niceSpinner.setTextColor(Color.RED);
        vh.niceSpinner.setBackgroundColor(noColor);
        LinkedList<String> data = new LinkedList<>(strings);
        vh.niceSpinner.attachDataSource(data);
        vh.niceSpinner.addOnItemClickListener(this);
        vh.btnDate.setOnClickListener(this);
        vh.btnLastDay.setOnClickListener(this);
        vh.btnNextDay.setOnClickListener(this);
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
        vh.btnDate.setText(TimeUtil.changeDateToShow(TimeUtil.getCurrentDateToDay(new Date())));
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                if (j > i) {
                    contents[i * 13 + j] = cards[i] + cards[j] + "s";
                    colors[i * 13 + j] = R.color.hui;
                } else if (j < i) {
                    contents[i * 13 + j] = cards[j] + cards[i] + "o";
                    colors[i * 13 + j] = R.color.hui;
                } else {
                    contents[i * 13 + j] = cards[j] + cards[i];
                    colors[i * 13 + j] = R.color.hui;
                }
            }
        }
        cardAdapter = new CardAdapter(context, contents, colors, textColors, getImageWidth());
        vh.cardList.setAdapter(cardAdapter);
        vh.cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView card = (TextView) view.findViewById(R.id.card);
                List<MyData> myDatas = DataSupport.where("pokerName=?", card.getText().toString()).find(MyData.class);
                MyData myData = null;
                if (myDatas.size() > 0) {
                    myData = myDatas.get(0);
                }
                double bbCount = 0;
                int playCount = 0;
                double maxBBCount = 0;
                double minBBCount = 0;
                if (myData != null) {
                    bbCount = myData.getBbCount();
                    playCount = myData.getPlayCount();
                    maxBBCount = myData.getMaxBbCount();
                    minBBCount = myData.getMinBbCount();
                }
                StringBuffer sb = new StringBuffer();
                sb.append("总战绩:").append(Constant.getDoubleString(1, bbCount)).append("bb#")
                        .append("总局数:").append(playCount).append("#")
                        .append("最大盈利:").append(Constant.getDoubleString(1, maxBBCount)).append("bb#")
                        .append("最大损失:").append(Constant.getDoubleString(1, minBBCount)).append("bb");
                String[] split = sb.toString().split("#");
                new AlertDialog.Builder(context)
                        .setItems(split, null)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    /*获取某一天的手牌记录*/
    public void getCardsInData(Date date) {
        clearColor();
        cardDataList = MyDataUtil.getOneDayMyData(date);
        changeNewData();
    }

    //更新数据
    private void changeNewData() {
        if (cardDataList.size() > 0) {
            double max = cardDataList.get(0).getBbCount();
            double min = cardDataList.get(0).getBbCount();
            for (int i = 0; i < cardDataList.size(); i++) {//获取最大最小值
                MyData myData = cardDataList.get(i);
                if (myData.getBbCount() > max)
                    max = myData.getBbCount();
                if (myData.getBbCount() < min)
                    min = myData.getBbCount();
            }
            //改变颜色
            for (int i = 0; i < cardDataList.size(); i++) {
                MyData myData = cardDataList.get(i);
                int colorIdx = getColorIdx(myData.getBbCount(), max, min);
                String pokerName = CardUtil.getPokerName(myData.getCard0(), myData.getCard1());
                if (!TextUtils.isEmpty(pokerName)) {
                    changeCardShow(getPosition(pokerName), colorIdx);
                }
            }
        }
        cardAdapter.notifyDataSetChanged();
    }

    /*先清除颜色*/
    private void clearColor() {
        if (cardDataList != null) {
            for (int i = 0; i < cardDataList.size(); i++) {
                MyData myData = cardDataList.get(i);
                if (!TextUtils.isEmpty(myData.getPokerName())) {
                    int position = getPosition(myData.getPokerName());
                    changeCardShow(position, 0);
                }
            }
        }
    }

    public void changeCardShow(int position, int colorIdx) {//改变单个手牌的背景颜色
        if (position == -1 || colorIdx > 8 || colorIdx < 0) {
            return;
        }
        colors[position] = eightColors[colorIdx];
        textColors[position] = eightTextColors[colorIdx];
    }

    //获取手牌背景颜色标记
    public int getColorIdx(double money, double max, double min) {
        if (money >= 0) {
            if (money >= 0 && money < max / 4) {
                return 5;
            } else if (money >= max / 4 && money < max / 2) {
                return 6;
            } else if (money >= max / 2 && money < max * 3 / 4) {
                return 7;
            } else {
                return 8;
            }
        } else if (money < 0) {
            if (money < 0 && money < min / 4) {
                return 1;
            } else if (money <= min / 4 && money > min / 2) {
                return 2;
            } else if (money <= min / 2 && money > min * 3 / 4) {
                return 3;
            } else {
                return 4;
            }
        }
        return 0;
    }

    public int getPosition(String cardStr) {
        for (int i = 0; i < contents.length; i++) {
            if (cardStr.equals(contents[i])) {
                return i;
            }
        }
        return -1;
    }

    private double getImageWidth() {
        // 尺子
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        // 把宽分成13份，
        return metrics.widthPixels / 13 - 5;
    }

    int dayC = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_last_day:
                showOneDayData(--dayC);
                break;
            case R.id.btn_next_day:
                showOneDayData(++dayC);
                break;
            case R.id.btn_date:
                datePickerDialog.setVibrate(false);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
                break;
        }
    }

    public void showOneDayData(int dayCount) {
        Date date = new Date(System.currentTimeMillis() + dayCount * 24 * 3600 * 1000);
        String dateToDay = TimeUtil.getCurrentDateToDay(date);
        vh.btnDate.setText(TimeUtil.changeDateToShow(dateToDay));
        getCardsInData(date);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(year, month, day);
        long dayCount = (instance.getTimeInMillis() - System.currentTimeMillis()) / (24 * 3600 * 1000);
        showOneDayData((int) dayCount);
        dayC = (int) dayCount;
    }

    int[] times = new int[]{0, 0, 3, 7, 15, 30, 90};

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        ReqData reqData = new ReqData();
        QueryPokerData data = new QueryPokerData();
        data.plattype = context.winIndex;
        data.userid = context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", "");
        if (TextUtils.isEmpty(data.userid))
            return;
        data.startdate = TimeUtil.getCurrentDateToDay(new Date(System.currentTimeMillis() - times[position] * 24 * 3600 * 1000));
        data.enddate = TimeUtil.getCurrentDateToDay(new Date());
        Gson gson = new Gson();
        String json = gson.toJson(data);
        reqData.setParam(json);
        reqData.setReqno(TimeUtil.getCurrentDateToMinutes(new Date()) + ActionsTool.disposeNumber());
        reqData.setReqid(context.getSharedPreferences(LoginActivity.PREF_FILE, Context.MODE_PRIVATE).getString("name", ""));
        HttpUtil.sendPostRequestData("querypokerdata", gson.toJson(reqData), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("PokerDetialActivity", "获取手牌记录失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Result result = GsonUtil.parseJsonWithGson(json, Result.class);
                Map<String, String> map = result.getRets();
                String pokers = map.get("listmydata");
                List<MyData> myDatas = new ArrayList<MyData>();
                Type listType = new TypeToken<List<MyData>>() {}.getType();
                myDatas = new Gson().fromJson(pokers, listType);
                if(myDatas.size()>0){
                    DataSupport.deleteAll(MyData.class);
                    for (int i = 0; i < myDatas.size(); i++) {
                        MyData myData = myDatas.get(i);
                        if(TextUtils.isEmpty(myData.getPokerName())){
                            myData.setPokerName(CardUtil.getPokerName(myData.getCard0(),myData.getCard1()));
                        }
                        myData.save();
                    }
                }
                Message msg = new Message();
                msg.arg1 = 1;
                msg.arg2 = position;
                handler.sendMessage(msg);
            }
        });
    }

    public void showPoker(int position){
        switch (position) {
            case 0:
                getCardsInData(new Date());
                break;
            case 1:
                clearColor();
                cardDataList = MyDataUtil.getAllMyData();
                changeNewData();
                break;
            case 2://3天
                clearColor();
                cardDataList = MyDataUtil.getSomeDayMyData(3);
                changeNewData();
                break;
            case 3://一周
                clearColor();
                cardDataList = MyDataUtil.getSomeDayMyData(7);
                changeNewData();
                break;
            case 4://15天
                clearColor();
                cardDataList = MyDataUtil.getSomeDayMyData(15);
                changeNewData();
                break;
            case 5://一个月
                clearColor();
                cardDataList = MyDataUtil.getSomeDayMyData(30);
                changeNewData();
                break;
            case 6://3个月
                clearColor();
                cardDataList = MyDataUtil.getSomeDayMyData(90);
                changeNewData();
                break;
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
           if(msg.arg1==1){
               showPoker(msg.arg2);
           }
        }
    };
    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.btn_last_day)
        Button btnLastDay;
        @BindView(R.id.btn_date)
        Button btnDate;
        @BindView(R.id.btn_next_day)
        Button btnNextDay;
        @BindView(R.id.note)
        TextView note;
        @BindView(R.id.niceSpinner)
        NiceSpinner niceSpinner;
        @BindView(R.id.linearLayout)
        LinearLayout linearLayout;
        @BindView(R.id.cardList)
        GridView cardList;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

