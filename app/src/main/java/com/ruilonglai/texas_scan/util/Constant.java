package com.ruilonglai.texas_scan.util;

/**
 * Created by WangShuai on 2017/5/3.
 */

public final class Constant {

    public final static String[] PLATFORM = new String[]{"dpq","pkbl-mtt","dyq"};
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


    public final static int SOCKET_ONE_HAND_LOG = 3;//saveParams
    public final static int SOCKET_BOARDS_AND_POKERS = 4;//boards,pokers
    public final static int SOCKET_EXIT = 5;//exit

    public final static int SOCKET_OPEN_WINDOW = 6;
    public final static int SOCKET_CLOSE_WINDOW = 7;

    public final static int SOCKET_PLATFORM_TEXASPOKER = 8;//德扑圈
    public final static int SOCKET_PLATFORM_NUTSPOKER = 9;//扑克部落
    public final static int SOCKET_PLATFORM_POKERFISHS = 10;//德友圈
    public final static int SOCKET_GET_TEMPLATE = 11;//获取重新初始化模板
    public final static int SOCKET_KNOW_NAME = 12;//识别出名字
    public final static int SOCKET_SEATCOUNT = 13;//seatCount;
    public final static int SOCKET_IS_WATCH = 14;//是否是旁观者模式

    public final static int TYPE_PERSON = 0;
    public final static int TYPE_OBSERVER = 1;

    public final static int TEST_SEATCOUNT = 0;//截图测试座位数
    public final static int TEST_NAME = 1;//截图测试名字

    /*悬浮窗位置*/
    public final static int[][][][] winPos = {
            {
                    {
                            {20, 110, 80, 40},
                            {200, 730, 350, -2},
                            {100, 110, 100, 50},
                            {60, 555, 100, 50},
                            {60, 795, 100, 50},
                            {60, 1020, 100, 50},
                            {80, 1120, 100, 50},
                            {570, 1120, 100, 50},
                            {570, 1020, 100, 50},
                            {570, 795, 100, 50},
                            {570, 555, 100, 50},
                            {200, 180, 100, 50},
                    },
                    {
                            {50, 230, 80, 40},
                            {200, 730, 350, -2},
                            {100, 130, 100, 50},
                            {60, 555, 100, 50},
                            {60, 795, 100, 50},
                            {60, 1020, 100, 50},
                            {430, 1150, 100, 50},
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
                            {60,  350, 100, 50},
                            {60,  760, 100, 50},
                            {430, 1150, 100, 50},
                            {600, 760, 100, 50},
                            {600, 350, 100, 50},
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
            }
    };
    public final static int TYPE_HAND = 0;
    public final static int TYPE_VPIP = 1;
    public final static int TYPE_PFR = 2;
    public final static int TYPE_3BET = 3;
    public final static int TYPE_CB = 4;
    public final static int TYPE_AF = 5;
    public final static int TYPE_F3BET= 6;
    public final static int TYPE_STL = 7;
    public final static int TYPE_FSTL = 8;

    public static String[] percentTypes = {
            "手数",
            "VPIP",
            "PFR",
            "3Bet",
            "CB",
            "AF",
            "F3Bet",
            "STL",
            "FSTL"
    };
    public static String[] percentContents = {
            "记录的总手数",
            "主动往彩池里投钱的频率,大小盲不计，除非在这两个位置再跟注或者加注",
            "翻牌前主动下注的频率",
            "面对有人加注,再次加注的频率,4bet,5bet等都算3bet",
            "翻牌前最后一个加注,在翻牌圈再加注的频率",
            "翻牌圈,转牌圈和河牌圈总的加注次数/总的跟注次数",
            "面对有人做3Bet动作后弃牌的频率",
            "在未加注过的底池，玩家在CO，按钮或者小盲注位置加注，称为偷盲",
            "玩家在面对偷盲企图时，弃牌的频率"
    };

}