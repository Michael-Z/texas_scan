package com.ruilonglai.texas_scan.ScreenShotUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShellUtils {
    public static final String COMMAND_SU       = "su";
    public static final String COMMAND_SH       = "sh";
    public static final String COMMAND_EXIT     = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    private Process process = null;
    private DataOutputStream os = null;

    public void Init(boolean isRoot){
        boolean exception = false;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);

            os = new DataOutputStream(process.getOutputStream());
        } catch (IOException e) {
            exception = true;
            e.printStackTrace();
        } catch (Exception e) {
            exception = true;
            e.printStackTrace();
        }
    }

    public void execCommand(String command) {
        boolean exception = false;
        BufferedReader errorResult = null;
        StringBuilder errorMsg = null;
        try {
            os.writeBytes(command);
            os.writeBytes(COMMAND_LINE_END);
            os.flush();
        } catch (IOException e) {
            exception = true;
            e.printStackTrace();
        } catch (Exception e) {
            exception = true;
            e.printStackTrace();
        }finally {
            try {
                if(errorResult!=null)
                    errorResult.close();
            }catch (Exception e){

            }
        }
    }

    public void close(){
        try{
            os.close();
        }
        catch(Exception e){

        }
        process.destroy();
    }

    public void WaitClose(){
        try{
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            process.waitFor();
        }
        catch(Exception e){

        }
        close();
    }
}
