package com.ruilonglai.texas_scan.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/4/12.
 */
public class BackData {

    private int btnIdx;
    private List<Player> seats;
    private int curDichi;
    private int dichi;
    private List<Integer> boards;
    private int m_ante;
    private int blinds;
    private int isStraddle;

    public class Player {
        private String name;
        private int winCount;
        private int loseCount;
        private int seatIdx;
        private int bet;
        private int card0;
        private int card1;
        private int money;
        private int hideCard=-1;

        public int getSeatIdx() {
            return seatIdx;
        }

        public void setSeatIdx(int seatIdx) {
            this.seatIdx = seatIdx;
        }

        public int getLoseCount() {
            return loseCount;
        }

        public void setLoseCount(int loseCount) {
            this.loseCount = loseCount;
        }

        public int getWinCount() {
            return winCount;
        }

        public void setWinCount(int winCount) {
            this.winCount = winCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getBet() {
            return bet;
        }

        public void setBet(int bet) {
            this.bet = bet;
        }

        public int getCard0() {
            return card0;
        }

        public void setCard0(int card0) {
            this.card0 = card0;
        }

        public int getCard1() {
            return card1;
        }

        public void setCard1(int card1) {
            this.card1 = card1;
        }

        public int getMoney() {
            return money;
        }

        public void setMoney(int money) {
            this.money = money;
        }

        public int getHideCard() {
            return hideCard;
        }

        public void setHideCard(int hideCard) {
            this.hideCard = hideCard;
        }

    }

    public int getBtnIdx() {
        return btnIdx;
    }

    public void setBtnIdx(int btnIdx) {
        this.btnIdx = btnIdx;
    }

    public List<Player> getSeats() {
        return seats;
    }

    public void setSeats(List<Player> seats) {
        this.seats = seats;
    }

    public int getCurDichi() {
        return curDichi;
    }

    public void setCurDichi(int curDichi) {
        this.curDichi = curDichi;
    }

    public int getDichi() {
        return dichi;
    }

    public void setDichi(int dichi) {
        this.dichi = dichi;
    }

    public List<Integer> getBoards() {
        return boards;
    }

    public void setBoards(List<Integer> boards) {
        this.boards = boards;
    }

    public int getBlinds() {
        return blinds;
    }

    public void setBlinds(int blinds) {
        this.blinds = blinds;
    }

    public int getM_ante() {
        return m_ante;
    }

    public void setM_ante(int m_ante) {
        this.m_ante = m_ante;
    }

    public int getIsStraddle() {
        return isStraddle;
    }

    public void setIsStraddle(int isStraddle) {
        this.isStraddle = isStraddle;
    }
}
