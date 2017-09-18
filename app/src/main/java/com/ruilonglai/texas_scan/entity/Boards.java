package com.ruilonglai.texas_scan.entity;

import java.util.List;

/**
 * Created by wgl on 2017/8/11.
 */

public class Boards {

    Seat seat;
    List<Integer> boards;

    public List<Integer> getBoards() {
        return boards;
    }

    public void setBoards(List<Integer> boards) {
        this.boards = boards;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
}
