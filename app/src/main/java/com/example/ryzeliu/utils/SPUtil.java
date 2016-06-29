package com.example.ryzeliu.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ryze.liu on 5/13/2016.
 */
public class SPUtil {

    private static final  String FILE_NAME = "share_data";
    public static final String KEY = "Bluetooth_address";
    public static void put(Context context,String address){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor et = sp.edit();
        et.putString(KEY,address);
        et.commit();
    }
    public static String getAddress(Context context){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        return sp.getString(KEY,null);
    }
}
