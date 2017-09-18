package com.ruilonglai.texas_scan.util;

import com.ruilonglai.texas_scan.entity.BackData;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wgl on 2017/4/18.
 */

public class FileIOUtil {

    public static void changeToStringSave(BackData data){
        String str = "";
        List<BackData.Player> seats = data.getSeats();
        str += data.getBtnIdx()+",";
        for (int i = 0; i < 9; i++) {
            if(i<seats.size()){
                str += seats.get(i).getMoney()+","+seats.get(i).getBet()+",";
            }else{
                str += "0,";
            }
        }
        str += data.getCurDichi()+","+data.getDichi()+",";
        for (int i = 0; i < 5; i++) {
            List<Integer> boards = data.getBoards();
            if(i<boards.size()){
                str += boards.get(i)+",";
            }else{
                str += "-1,";
            }
        }
        String substring = str.substring(0, str.length() - 1);//去掉最后的逗号
        saveToFile(getTime()+","+substring);
    }
    public static void saveToFile(String str){
        FileOutputStream fos2 = null;
        try {
            File file = new File("/mnt/sdcard/desk_scan/");
            if(!file.exists()){
                file.mkdirs();
            }
            File file1 = new File("/mnt/sdcard/desk_scan/log.txt");
            if(!file1.exists()){
                file1.createNewFile();
            }
            fos2 = new FileOutputStream("/mnt/sdcard/desk_scan/log.txt", true);// 第二個参数为true表示程序每次运行都是追加字符串在原有的字符上
            fos2.write(str.getBytes());// 写数据
            fos2.write("\r\n".getBytes());// 写入一个换行
            fos2.close(); // 释放资源
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void writeLine(){
        FileOutputStream fos2 = null;
        try {
            File file = new File("/mnt/sdcard/desk_scan/");
            if(!file.exists()){
                file.mkdirs();
            }
            File file1 = new File("/mnt/sdcard/desk_scan/log.txt");
            if(!file1.exists()){
                file1.createNewFile();
            }
            fos2 = new FileOutputStream("/mnt/sdcard/desk_scan/log.txt", true);// 第二個参数为true表示程序每次运行都是追加字符串在原有的字符上
            fos2.write("---------------------------------------------------------------------------------------".getBytes());
            fos2.write("\r\n".getBytes());// 写入一个换行
            fos2.close(); // 释放资源
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    public static String getTime(){
        Date date = new Date();
        long time = date.getTime();
        String str = "";
        str = ((long)Math.floor(time/3600/1000)%24)+
                ":"+((long)Math.floor(time/60/1000)%60)+" "+((long)Math.floor(time/1000)%60)+"s"+time%1000+"ms";
        return str;
    }
}
