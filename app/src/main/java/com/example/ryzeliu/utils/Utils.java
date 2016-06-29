package com.example.ryzeliu.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by ryze.liu on 5/12/2016.
 */
public class Utils {
    public static final String BL_UUID = "00001c00-d102-11e1-9b23-000efb0000a5";
    public static final String BL_READ_UUID = "00001C0F-D102-11E1-9B23-000EFB0000A5";
    public static final String BL_WRITE_UUID = "00001C01-D102-11E1-9B23-000EFB0000A5";
    /**
     * Convert pixel to dp. Preserve the negative value as it's used for representing
     * MATCH_PARENT(-1) and WRAP_CONTENT(-2).
     * Ignore the round error that might happen in dividing the pixel by the density.
     *
     * @param context the context
     * @param pixel the value in pixel
     * @return the converted value in dp
     */
    public static int pixelToDp(Context context, int pixel) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return pixel < 0 ? pixel : Math.round(pixel / displayMetrics.density);
    }

    /**
     * Convert dp to pixel. Preserve the negative value as it's used for representing
     * MATCH_PARENT(-1) and WRAP_CONTENT(-2).
     *
     * @param context the context
     * @param dp the value in dp
     * @return the converted value in pixel
     */
    public static int dpToPixel(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return dp < 0 ? dp : Math.round(dp * displayMetrics.density);
    }

    /**
     * 将16进制 转换成10进制
     *
     * @param str
     * @return
     */
    public static String print10(String str) {

        StringBuffer buff = new StringBuffer();
        String array[] = str.split(" ");
        for (int i = 0; i < array.length; i++) {
            int num = Integer.parseInt(array[i], 16);
            buff.append(String.valueOf((char) num));
        }
        return buff.toString();
    }

    /**
     * byte转16进制
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    public static final int LEFT = 1;
    public static final int RIGHT =2;
    public static final int CENTER = 3;
    public static final int UP = 4;
    public static final int DOWN =5;

    /**
     *get the control  command
     * @param command the control command
     * @param status press or release
     */

    public static byte[] controlCommand(int command,boolean press){

            byte[] cmd = new byte[2];
                cmd[0] = (byte)0x97;
             switch (command){
                 case 1:
                     cmd[1] = (press == true)?(byte)0x00 : 0x01;//left
                     break;
                 case 2:
                     cmd[1] = (press == true)?(byte)0x02 : 0x03;//right
                     break;
                 case 3:
                     cmd[1] = (press == true)?(byte)0x08 : 0x09;//center
                     break;
                 case 4:
                     cmd[1] = (press == true)?(byte)0x04 : 0x05;//up
                     break;
                 case 5:
                     cmd[1] = (press == true)?(byte)0x06 : 0x07;//down
                     break;
             }
            return cmd;
    }
    /**
     * 将16进制的字符串转换为字节数组
     *
     * @param message
     * @return 字节数组
     */
    public static byte[] getHexBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }
}
