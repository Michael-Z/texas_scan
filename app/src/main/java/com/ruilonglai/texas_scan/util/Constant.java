package com.ruilonglai.texas_scan.util;

import com.ruilonglai.texas_scan.entity.PlayerData;

/**
 * Created by WangShuai on 2017/5/3.
 */

public final class Constant {

    public final static String[] PLATFORM = new String[]{"dpq","dyq","pkbl-mtt","pkbl-sng"};
    public final static String[] APPNAMES = {"德扑圈","德友圈","扑克部落MTT","扑克部落SNG"};
    public final static String[] seatFlags = {"BTN", "SB", "BB", "UTG", "UTG+1", "MP", "MP+1", "HJ", "CO"};

    //一手数据中的输赢平
    public final static int FLAG_SELF = 0;
    public final static int FLAG_WATCH = 1;

    public final static int ACTION_CHECK = 0;//让牌
    public final static int ACTION_CALL = 1;//跟进
    public final static int ACTION_BET = 2;//下注
    public final static int ACTION_FOLD = 3;//弃牌
    public final static int ACTION_ALLIN = 4;//全压
    public final static int ACTION_RAISE = 5;//加注
    public final static int ACTION_RERAISE = 6;//再加注

    public final static int ROUND_PREFLOP = 0;//翻牌前
    public final static int ROUND_FLOP = 3;//翻牌圈
    public final static int ROUND_TURN = 4;//转牌圈
    public final static int ROUND_RIVER = 5;//河牌圈

    public final static int MSG_FROM_CLIENT = 0;//客户端信息
    public final static int MSG_FROM_SERVER = 1;//服务器信息

    public final static int SOCKET_RESTART_MAIN_PROCESS = 0;//重启Main进程
    public final static int SOCKET_ONE_HAND_LOG = 3;//saveParams
    public final static int SOCKET_BOARDS_AND_POKERS = 4;//boards,pokers
    public final static int SOCKET_EXIT = 5;//exit

    public final static int SOCKET_OPEN_WINDOW = 6;
    public final static int SOCKET_CLOSE_WINDOW = 7;

    public final static int SOCKET_PLATFORM_TEXASPOKER = 8;//德扑圈
    public final static int SOCKET_PLATFORM_POKERFISHS = 9;//德友圈
    public final static int SOCKET_PLATFORM_NUTSPOKER = 10;//扑克部落
    public final static int SOCKET_PLATFORM_NUTSPOKER_SNG = 11;//扑克部落SNG
    public final static int SOCKET_KNOW_NAME = 12;//识别出名字
    public final static int SOCKET_SEATCOUNT = 13;//seatCount;
    public final static int SOCKET_IS_WATCH = 14;//是否是旁观者模式
    public final static int SOCKET_GET_TEMPLATE = 15;//获取重新初始化模板
    public final static int SOCKET_SEATCOUNT_CHANGE = 16;//座位数变化
    public final static int SOCKET_SCANNAME = 17;//调用接口识别名字
    public final static int SOCKET_UPDATE_NAME = 18;//修改名字

    public final static int TYPE_PERSON = 0;
    public final static int TYPE_OBSERVER = 1;

    public final static int TEST_SEATCOUNT = 0;//截图测试座位数
    public final static int TEST_NAME = 1;//截图测试名字

    /*悬浮窗位置1080*1920*/
    public final static int[][][][] winPos_1080 = {
            {
                    {
                            {30, 165, 80, 40},
                            {300, 995, 350, -2},
                            {160, 165, 100, 100},
                            {90, 700, 100, 100},
                            {90, 1040, 100, 100},
                            {110, 1415, 100, 100},
                            {140, 1560, 100, 100},
                            {875, 1560, 100, 100},
                            {865, 1415, 100, 100},
                            {865, 1040, 100, 100},
                            {865, 700, 100, 100},
                            {300, 270, 100, 100},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {60, 555, 100, 50},
                            {60, 795, 100, 50},
                            {60, 1020, 100, 50},
                            {450, 1150, 100, 50},
                            {570, 1020, 100, 50},
                            {570, 795, 100, 50},
                            {570, 555, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {15,  580, 100, 50},
                            {15,  970, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {600, 970, 100, 50},
                            {600, 580, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  330, 100, 50},
                            {30,  980, 100, 50},
                            {450, 1150, 100, 50},
                            {600, 980, 100, 50},
                            {600, 330, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {20, 110, 80, 40},
                            {200, 730, 350, -2},
                            {100, 110, 100, 50},
                            {420, 1150, 100, 50},
                            {200, 180, 100, 50},
                    },
            },
            {
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {100, 1120, 100, 50},
                            {560, 1120, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {100, 1120, 100, 50},
                            {560, 1120, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50},
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50},
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {420, 1150, 100, 50},
                            {100, 130, 100, 50}
                    },
            },
            {
                    {
                            {30, 165, 80, 40},
                            {300, 995, 350, -2},
                            {160, 165, 100, 100},
                            {90, 700, 100, 100},
                            {90, 1040, 100, 100},
                            {110, 1410, 100, 100},
                            {140, 1560, 100, 100},
                            {875, 1560, 100, 100},
                            {865, 1410, 100, 100},
                            {865, 1040, 100, 100},
                            {865, 700, 100, 100},
                            {300, 270, 100, 100},
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50}
                    }
            },
            {
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {20, 350, 100, 50},
                            {20, 580, 100, 50},
                            {20, 810, 100, 50},
                            {240, 1150, 100, 50},
                            {380, 1150, 100, 50},
                            {600, 810, 100, 50},
                            {600, 580, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {15,  580, 100, 50},
                            {15,  970, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {600, 970, 100, 50},
                            {600, 580, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  580, 100, 50},
                            {30,  950, 100, 50},
                            {450, 1150, 100, 50},
                            {600, 950, 100, 50},
                            {600, 580, 100, 50},
                            {100, 130, 100, 50}
                    }
            }
    };/*悬浮窗位置720*1280*/
    public final static int[][][][] winPos = {
            {
                    {
                            {20, 110, 80, 40},
                            {190, 500, 350, -2},
                            {100, 110, 100, 50},
                            {60, 558, 100, 50},
                            {60, 798, 100, 50},
                            {60, 1030, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {570, 1030, 100, 50},
                            {570, 798, 100, 50},
                            {570, 558, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {190, 500, 350, -2},
                            {100, 130, 100, 50},
                            {60, 558, 100, 50},
                            {60, 798, 100, 50},
                            {60, 1030, 100, 50},
                            {450, 1150, 100, 50},
                            {570, 1030, 100, 50},
                            {570, 798, 100, 50},
                            {570, 558, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {190, 500, 350, -2},
                            {100, 130, 100, 50},
                            {15,  585, 100, 50},
                            {15,  970, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {600, 970, 100, 50},
                            {600, 585, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {190, 500, 350, -2},
                            {100, 130, 100, 50},
                            {30,  350, 100, 50},
                            {30,  980, 100, 50},
                            {450, 1150, 100, 50},
                            {600, 980, 100, 50},
                            {600, 350, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {20, 110, 80, 40},
                            {190, 500, 350, -2},
                            {100, 110, 100, 50},
                            {420, 1150, 100, 50},
                            {200, 180, 100, 50},
                    },
            },
            {
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {100, 1120, 100, 50},
                            {560, 1120, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {100, 1120, 100, 50},
                            {560, 1120, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50},
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50},
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {420, 1150, 100, 50},
                            {100, 130, 100, 50}
                    },
            },
            {
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {20, 350, 100, 50},
                            {20, 580, 100, 50},
                            {20, 810, 100, 50},
                            {240, 1150, 100, 50},
                            {380, 1150, 100, 50},
                            {600, 810, 100, 50},
                            {600, 580, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  400, 100, 50},
                            {30,  790, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 790, 100, 50},
                            {600, 400, 100, 50},
                            {100, 130, 100, 50}
                    }
            },
            {
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {20, 350, 100, 50},
                            {20, 580, 100, 50},
                            {20, 810, 100, 50},
                            {240, 1150, 100, 50},
                            {380, 1150, 100, 50},
                            {600, 810, 100, 50},
                            {600, 580, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 210, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {32, 350, 100, 50},
                            {32, 600, 100, 50},
                            {32, 845, 100, 50},
                            {420, 1150, 100, 50},
                            {600, 845, 100, 50},
                            {600, 600, 100, 50},
                            {600, 350, 100, 50},
                            {100, 130, 100, 50}
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {15,  580, 100, 50},
                            {15,  970, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {600, 970, 100, 50},
                            {600, 580, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {30,  580, 100, 50},
                            {30,  950, 100, 50},
                            {450, 1150, 100, 50},
                            {600, 950, 100, 50},
                            {600, 580, 100, 50},
                            {100, 130, 100, 50}
                    }
            }
    };
    /*概率类型*/
    public final static int TYPE_HAND = 0;
    public final static int TYPE_VPIP = 1;
    public final static int TYPE_PFR = 2;
    public final static int TYPE_3BET = 3;
    public final static int TYPE_CB = 4;
    public final static int TYPE_AF = 5;
    public final static int TYPE_F3BET= 6;
    public final static int TYPE_STL = 7;
    public final static int TYPE_FSTL = 8;
    public final static int TYPE_FCB = 9;
    public final static int TYPE_FFLOP = 10;
    public final static int TYPE_FTURN = 11;
    public final static int TYPE_FRIVER = 12;


    public static String[] percentTypes = {
            "手数",
            "VPIP",
            "PFR",
            "3Bet",
            "CB",
            "AF",
            "F3Bet",
            "STL",
            "FSTL",
            "FCB",
            "FFlop",
            "FTurn",
            "FRiver"
    };
    public static String[] percentContents = {
            "记录的总手数",
            "主动进池的频率（大盲或straddle位过牌入池不计）",
            "翻牌前主动下注的频率",
            "面对有人加注,再次加注的频率,4bet,5bet等都算3bet",
            "翻牌前最后一个加注,在翻牌圈再加注的频率",
            "翻牌圈,转牌圈和河牌圈总的加注次数/总的跟注次数",
            "面对有人做3Bet动作后弃牌的频率",
            "在未加注过的底池，玩家在CO，按钮或者小盲注位置加注，称为偷盲",
            "玩家在面对偷盲企图时，弃牌的频率",
            "翻牌前的加注者在翻拍圈接着下注后跟进来的人弃牌",
            "在翻牌圈,弃牌的频率",
            "在转牌圈,弃牌的频率",
            "在河牌圈,弃牌的频率"
    };
    //保留几位小数
    public static String getDoubleString(int num,double count){
      return  String.format("%."+num+"f", count);
    }

    /*获取相应类型的概率*/
    public static String getPercent(PlayerData player, int type) {
        String percent = "－";
        int playCount = player.getPlayCount();
        switch (type) {
            case Constant.TYPE_HAND:
                if (playCount >= 1000) {
                    percent = playCount / 1000 + "K+";
                } else {
                    percent = "(" + playCount + ")";
                }
                break;
            case Constant.TYPE_VPIP:
                if (playCount != 0)
                    percent = disposeBadNumber(player.getJoinCount() * 100 / playCount) + "";
                break;
            case Constant.TYPE_PFR:
                if (playCount != 0)
                    percent = disposeBadNumber(player.getPfrCount() * 100 / playCount) + "";
                break;
            case Constant.TYPE_3BET:
                if (player.getFaceOpenCount() != 0)
                    percent = disposeBadNumber(player.getBet3Count() * 100 / player.getFaceOpenCount()) + "";
                break;
            case Constant.TYPE_CB:
                if (player.getLastRaiseCount() != 0)
                    percent = disposeBadNumber(player.getCbCount() * 100 / player.getLastRaiseCount()) + "";
                break;
            case Constant.TYPE_AF:
                if (player.getCallCount() != 0) {
                    double af = player.getRaiseCount() / Double.valueOf(player.getCallCount());
                    if (af > 10) {
                        af = af / 2;
                    }
                    percent = String.format("%.1f", af);
                }

                break;
            case Constant.TYPE_F3BET:
                if (player.getFace3BetCount() != 0)
                    percent = disposeBadNumber(player.getFold3BetCount() * 100 / player.getFace3BetCount()) + "";
                break;
            case Constant.TYPE_STL:
                if (player.getStlPosCount() != 0)
                    percent = disposeBadNumber(player.getStlCount() * 100 / player.getStlPosCount()) + "";
                break;
            case Constant.TYPE_FSTL:
                if (player.getFaceStlCount() != 0)
                    percent = disposeBadNumber(player.getFoldStlCount() * 100 / player.getFaceStlCount()) + "";
                break;
            case Constant.TYPE_FCB:
                if (player.getFaceCbCount() != 0)
                    percent = disposeBadNumber(player.getFoldCbCount() * 100 / player.getFaceCbCount()) + "";
                break;
            case Constant.TYPE_FFLOP:
                if (player.getFlopCount() != 0 && playCount != 0)
                    percent = disposeBadNumber(player.getFoldFlopCount() * 100 / player.getFlopCount()) + "";
                break;
            case Constant.TYPE_FTURN:
                if (player.getTurnCount() != 0 && playCount != 0)
                    percent = disposeBadNumber(player.getFoldTurnCount() * 100 / player.getTurnCount()) + "";
                break;
            case Constant.TYPE_FRIVER:
                if (player.getRiverCount() != 0 && playCount != 0)
                    percent = disposeBadNumber(player.getFoldRiverCount() * 100 / player.getRiverCount()) + "";
                break;
        }
        return percent;
    }

    public static int disposeBadNumber(int num){
        if(num>100)
            num = 100;
        return num;
    }

}
