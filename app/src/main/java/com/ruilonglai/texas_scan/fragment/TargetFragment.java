package com.ruilonglai.texas_scan.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.ruilonglai.texas_scan.R;
import com.ruilonglai.texas_scan.adapter.CardAdapter;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * Created by wangshuai on 2017/5/17.
 */

public class TargetFragment extends Fragment {
    @BindView(R.id.handsList)
    GridView handsList;
    @BindColor(R.color.red)
    int red;
    @BindColor(R.color.hui)
    int hui;
    @BindColor(R.color.nocolor)
    int noColor;
    @BindView(R.id.niceSpinner)
    NiceSpinner niceSpinner;
    @BindView(R.id.spinner_notice)
    TextView spinnerNotice;
    CardAdapter adapter;
    private int[] colors = new int[169];
    private int[] textColors = new int[169];
    private String[] contents = new String[169];
    String[] cards = {"A", "K", "Q", "J", "T", "9", "8", "7", "6", "5", "4", "3", "2"};
    List<String> strings = Arrays.asList("1%", "2%", "5%", "7%", "10%", "15%", "20%", "28%", "50%");
    String[][] arr = {
            {"KK", "AA"},
            {"JJ", "QQ", "KK", "AA", "AKo", "AKs"},
            {"99", "TT", "JJ", "QQ", "KK", "AA", "AQo", "AKo", "AQs", "AKs"},
            {"88", "99", "TT", "JJ", "QQ", "KK", "AA", "AJo", "AQo", "AKo", "ATs", "AJs", "AQs", "AKs"},
            {"77", "88", "99", "TT", "JJ", "QQ", "KK", "AA", "ATo", "AJo", "AQo", "AKo", "A8s", "A9s", "ATs", "AJs", "AQs", "AKs", "KQo", "KQs"},
            {"66", "77", "88", "99", "TT", "JJ", "QQ", "KK", "AA", "A8o", "A9o", "ATo", "AJo", "AQo", "AKo", "A5s", "A6s", "A7s", "A8s", "A9s", "ATs", "AJs", "AQs", "AKs", "KJo", "KQo", "KTs", "KJs", "KQs", "QJs"},
            {"22", "33", "44", "55", "66", "77", "88", "99", "TT", "JJ", "QQ", "KK", "AA", "A2o", "A3o", "A4o", "A5o", "A6o", "A7o", "A8o", "A9o", "ATo", "AJo", "AQo", "AKo", "A2s", "A3s", "A4s", "A5s", "A6s", "A7s", "A8s", "A9s", "ATs", "AJs", "AQs", "AKs", "KTo", "KJo", "KQo","KTs", "KJs", "KQs", "K9s", "KTs", "KJs", "KQs", "QJs", "JTo", "QTs", "QJs", "J9s", "JTs", "T9s", "98s", "87s", "76s"},
            {"22", "33", "44", "55", "66", "77", "88", "99", "TT", "JJ", "QQ", "KK", "AA", "A2o", "A3o", "A4o", "A5o", "A6o", "A7o", "A8o", "A9o", "ATo", "AJo", "AQo", "AKo", "A2s", "A3s", "A4s", "A5s", "A6s", "A7s", "A8s", "A9s", "ATs", "AJs", "AQs", "AKs","KTo", "KJo", "KQo","KTs", "KJs", "KQs", "K9s", "KTs", "KJs", "KQs", "QJs", "JTo", "QTs", "QJs", "J9s", "JTs", "T9s", "98s", "87s", "76s", "QTo", "QJo", "JTo", "QTs", "QJs", "JTs"},
            {"22", "33", "44", "55", "66", "77", "88", "99", "TT", "JJ", "QQ", "KK", "AA", "A2o","A2s", "A3o", "A3s", "A4o", "A4s", "A5o","A5s", "A6o", "A6s", "A7o","A7s", "A8o", "A8s", "A9o", "A9s", "ATo", "ATs", "AJo", "AJs", "AQo", "AQs", "AKo", "AKs", "K4o", "K5o", "K6o", "K7o", "K8o", "K9o", "KTo", "KJo", "KQo", "K2s", "K3s", "K4s", "K5s", "K6s", "K7s", "K8s", "K9s", "KTs", "KJs", "KQs", "Q6o", "Q7o", "Q8o", "Q9o", "QTo", "QJo", "Q3s", "Q4s", "Q5s", "Q6s", "Q7s", "Q8s", "Q9s", "QTs", "QJs", "J8o", "J9o", "JTo", "J7s", "J8s", "J9s", "JTs", "T9o", "98o", "65o", "54s", "T7s", "T8s", "T9s", "97s", "98s", "86s", "87s", "75s", "76s"}
    };
    String[] notes = {
            "KK+",
            "JJ+,AK",
            "99+,AQ+",
            "88+,AJo+,ATs+",
            "77+,ATo+,A8s+,KQ",
            "66+,A8o+,A5s+,KJo,KTs+,QJs",
            "55+,A7o+,A4o+,KT+,K9s,QJo,QTs+,\nJTo,J9s+,T9s,98s,87s,76s",
            "任何对子,任何A牌,任何高张(T以上)",
            "22+,A2+,K4o+,K2s+,Q6o+,Q3s+,J8o+,J7s+,\nT9o,T7s+,98o,97s+,87o,86s+,75s+,65s,54s"
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_target, container, false);
        ButterKnife.bind(this, view);

        niceSpinner.setTextColor(Color.RED);
        niceSpinner.setBackgroundColor(noColor);
        LinkedList<String> data = new LinkedList<>(strings);
        niceSpinner.attachDataSource(data);
        niceSpinner.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                spinnerNotice.setText(notes[position]);
                setCards(position);
            }
        });
        spinnerNotice.setText(notes[0]);
        initView();
        setCards(0);
        return view;
    }

    @OnItemClick(R.id.handsList)//手牌点击事件
    public void selectOneCard(View view, int position) {
        TextView text = (TextView) view.findViewById(R.id.card);
        if (colors[position] == R.color.hui) {
            text.setBackgroundColor(red);
            colors[position] = R.color.red;
        } else if (colors[position] == R.color.red) {
            text.setBackgroundColor(hui);
            colors[position] = R.color.hui;
        }
    }
    public void setCards(int position){
        initView();
        for (int i = 0; i < arr[position].length; i++) {
            int position1 = getPosition(arr[position][i]);
            colors[position1] = R.color.red;
        }
        if(adapter==null){
            adapter = new CardAdapter(getActivity(), contents, colors, textColors, getImageWidth());
            handsList.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }
    }
    public void initView() {
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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    public int getPosition(String pokerName){
        int card1=-1;
        int card2=-1;
        if(pokerName.length()>=2){
            for (int i = 0; i < cards.length; i++) {
                if(pokerName.substring(0,1).equals(cards[i])){
                    card1 = i;
                }
                if(pokerName.substring(1,2).equals(cards[i])){
                    card2 = i;
                }
                if(card1!=-1 && card2!= -1){
                    break;
                }
            }
            if(pokerName.contains("s") || pokerName.length()==2){
                return card1*13+card2;
            }else if(pokerName.contains("o")){
                return card2*13+card1;
            }
        }
       return -1;
    }
    private double getImageWidth() {
        // 尺子
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);
        // 把宽分成13份，
        return metrics.widthPixels / 13 - 5;
    }
}
