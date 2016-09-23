package com.example.ryzeliu.bluetoothapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryzeliu.services.BluetoothCallback;
import com.example.ryzeliu.utils.LogUtil;
import com.example.ryzeliu.utils.Utils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryze.liu on 5/18/2016.
 */
public class ControlActivity extends AppCompatActivity{
    private static final  String TAG = "ControlActivity";
    private Button bt_center, bt_left, bt_up, bt_right, bt_down;
    private CheckBox[] mCheckBoxes;
    private BluetoothDevice device;
    private Intent intent;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothCallback mBluetoothCallback;
    private int mStatus;
    private byte[] cmd;
    private ProgressDialog mDialog;
    public static final String LEVEL_KEY = "PRESSURE_LEVEL";
    public static final int GET_PRESSURE_LEVEL = 1;
    private RelativeLayout mRelativeLayout;
    public static final int GAS_RELEASE = 7;
    public static final int GAS_CHARGING = 8;
    public static final int GAS_CHARGING_OR_RELEASE_FINISH = 9;
    public static final int GAS_LIGHT_OFF = 10;
    public static final int GAS_LIGHT_ON = 11;
    public static final int GAS_MAX= 6;
    public static final int GAS_MIN = 0;
    public int bed_status = 0;
    public int temp = -1;
    public final Timer timer = new Timer();
    public Message msg;
    private MyTask task;
    private TextView tv_sendData,tv_getData;
    private final int RIGHT = 3;
    private final int LEFT = 4;
    private int side = 0;
    private int count = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);
        mBluetoothCallback = new BluetoothCallback(this, mHandler);
        initView();
        intent = getIntent();
        if (intent != null) {
            device = intent.getParcelableExtra(MainActivity.DEVICE);
            if (device != null) {
                mBluetoothGatt = device.connectGatt(this, false, mBluetoothCallback);
                mStatus = BluetoothProfile.STATE_CONNECTING;
                mDialog.show();
            }
        }

        ProcessBuilder pr = new ProcessBuilder();
        try {
            pr.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initView() {
        mRelativeLayout = (RelativeLayout) findViewById(R.id.layout_level);
        mCheckBoxes = new CheckBox[mRelativeLayout.getChildCount()];
        bt_center = (Button) findViewById(R.id.bt_center);
        bt_left = (Button) findViewById(R.id.bt_left);
        bt_up = (Button) findViewById(R.id.bt_up);
        bt_right = (Button) findViewById(R.id.bt_right);
        bt_down = (Button) findViewById(R.id.bt_down);
        tv_getData = (TextView) findViewById(R.id.tv_getData);
        tv_sendData = (TextView) findViewById(R.id.tv_sendData);
        setUnconnectedButtonBackground();

        mDialog = createLoadingDialog(this);
        bt_down.setOnTouchListener(mTouchListener);
        bt_left.setOnTouchListener(mTouchListener);
        bt_center.setOnTouchListener(mTouchListener);
        bt_right.setOnTouchListener(mTouchListener);
        bt_up.setOnTouchListener(mTouchListener);
        bt_up.setOnLongClickListener(onLongClickListener);
        bt_down.setOnLongClickListener(onLongClickListener);
        bt_center.setOnLongClickListener(onLongClickListener);
        for (int i =0 ;i < mRelativeLayout.getChildCount();i++){
            mCheckBoxes[i] = (CheckBox) mRelativeLayout.getChildAt(i);
        }
    }

    /**
     * if not connected the remote devices BL
     */
    void setUnconnectedButtonBackground(){

        bt_left.setBackgroundResource(R.drawable.bt_ppj_left);
        bt_up.setBackgroundResource(R.drawable.bt_ppj_up);
        bt_center.setBackgroundResource(R.drawable.bt_ppj_middle);
        bt_right.setBackgroundResource(R.drawable.bt_ppj_right);
        bt_down.setBackgroundResource(R.drawable.bt_ppj_down);

    }

    /**
     * if connected the remote device BL
     */
    void setConnectedButtonBackground(){

        bt_left.setBackgroundResource(R.drawable.bt_ppj_left_glow);
        bt_up.setBackgroundResource(R.drawable.button_up);
        bt_center.setBackgroundResource(R.drawable.button_center);
        bt_right.setBackgroundResource(R.drawable.bt_ppj_right_glow);
        bt_down.setBackgroundResource(R.drawable.button_down);

    }
    /**
     * progressDialog
     * @param context
     * @return
     */
    public static ProgressDialog createLoadingDialog(Context context) {
        ProgressDialog mDialog = new ProgressDialog(context);
        mDialog.setMessage("Connecting . . . . .");
        return  mDialog;
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == BluetoothProfile.STATE_CONNECTED){
                mStatus = BluetoothProfile.STATE_CONNECTED;
                Toast.makeText(getApplicationContext(),device.getName()+" was connected",Toast.LENGTH_LONG).show();
                mDialog.dismiss();
                setConnectedButtonBackground();

            }
            else if (msg.what == BluetoothProfile.STATE_DISCONNECTED){
                mStatus = BluetoothProfile.STATE_DISCONNECTED;
                Toast.makeText(getApplicationContext(),device.getName()+" was disconnected",Toast.LENGTH_LONG).show();
                setUnconnectedButtonBackground();
                setLightOff();
            }
            else if (msg.what == GET_PRESSURE_LEVEL){
                int level = msg.getData().getInt(LEVEL_KEY);
                LogUtil.d(TAG, "GAS  LEVEL:" + level);
                if (bed_status == GAS_CHARGING || bed_status == GAS_RELEASE)
                    return;
                showPressureLevel(level);
                temp = level;
            }
       /*     else if (msg.what == GAS_CHARGING){
                LogUtil.d(TAG, "BED  CHARGING");
                setGasChargingLight();
            }else if (msg.what == GAS_CHARGING_OR_RELEASE_FINISH){
               // LogUtil.d(TAG, "FINISH CHARGING OR RESEASE");
               // showPressureLevel(5);
            }else if (msg.what == GAS_LIGHT_OFF){
                setLightOff();
            }else if (msg.what == GAS_RELEASE){
                LogUtil.d(TAG,"BED  RELEASE");
                setGasReleaseLight();
            }else if (msg.what == GAS_LIGHT_ON){
                //TODO
                setGasLightOn();
            }*/
            else if (msg.what == RIGHT){
                side = RIGHT;
                bt_left.setBackgroundResource(R.drawable.bt_ppj_left_glow);
                bt_right.setBackgroundResource(R.drawable.bt_ppj_right_glow_pressed);
            }
            else if (msg.what == LEFT){
                side = LEFT;
                bt_left.setBackgroundResource(R.drawable.bt_ppj_left_glow_pressed);
                bt_right.setBackgroundResource(R.drawable.bt_ppj_right_glow);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBluetoothCallback.closeConnected();
        super.onDestroy();
    }
    /**
     * view on touch event
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
         /*   if (id == R.id.bt_left && side == LEFT)
                return false;
            if (id == R.id.bt_right && side == RIGHT)
                return false;*/
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    LogUtil.d(TAG, "side :"+side);
                    if (id==R.id.bt_left || id == R.id.bt_right) {
                        LogUtil.d(TAG, "View.OnTouchListener DOWN");
                        cmd = setCommand(id, true);
                        mBluetoothCallback.writeCharacteristic(cmd);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    try {
                        if (id == R.id.bt_left || id == R.id.bt_right) {
                            Thread.sleep(100);
                            LogUtil.d(TAG, "View.OnTouchListener UP");
                            cmd = setCommand(id, false);
                            mBluetoothCallback.writeCharacteristic(cmd);
                            msg = mHandler.obtainMessage();
                            msg.what = id == R.id.bt_left ? LEFT : RIGHT;
                            msg.sendToTarget();
                        }else{
                            if (down){
                                LogUtil.d(TAG,"View LONG CLICK EVENT UP");
                                cmd = setCommand(id, false);
                                mBluetoothCallback.writeCharacteristic(cmd);
                             //   msg = mHandler.obtainMessage();
                            //    msg.what = GAS_CHARGING_OR_RELEASE_FINISH;
                            //    bed_status = GAS_CHARGING_OR_RELEASE_FINISH;
                             //   msg.sendToTarget();
                              //  timer.purge();
                              // task.cancel();
                                down = false;
                            }else {
                                if (count == 0) {
                                    count = 1;
                                    mHandler.postDelayed(resetCountRunnable, 3000);
                                }
                            }
                        }
    } catch (Exception e) {
    e.printStackTrace();
    }
    break;
    }
    return false;
    }
    };
        private Runnable resetCountRunnable = new Runnable() {
            @Override
            public void run() {
                count = 0;
            }
        };
    /**
     *
     * @param id view id
     * @param press press or not
     * @return
     */
    private byte[] setCommand(int id, boolean press) {
            byte[] command = null;
        switch (id) {
            case R.id.bt_center:
                command = Utils.controlCommand(Utils.CENTER, press);
                break;
            case R.id.bt_left:
                command = Utils.controlCommand(Utils.LEFT, press);
                break;
            case R.id.bt_up:
                command =Utils.controlCommand(Utils.UP, press);
                break;
            case R.id.bt_right:
                command =Utils.controlCommand(Utils.RIGHT, press);
                break;
            case R.id.bt_down:
                command =Utils.controlCommand(Utils.DOWN, press);
                break;
        }
             return command;
    }

    /**
     * button  long click event
     */
    private boolean down = false;
    public View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (count == 0)
                return true;
            mHandler.removeCallbacks(resetCountRunnable);
            LogUtil.d(TAG,"ON LONG CLICK EVENT DOWN");
            cmd = setCommand(v.getId(), true);
            mBluetoothCallback.writeCharacteristic(cmd);
            down = true;
          //  showLight(v);
            count = 0;
            return false;
        }
    };


    public void showLight(View v){
        if (v.getId() == R.id.bt_up) {
            bed_status = GAS_CHARGING;
    }
    else if (v.getId() == R.id.bt_down){
        bed_status = GAS_RELEASE;
    }
        task = new MyTask();
        timer.schedule(task,0,500);

    }
/**
     * this task for button longclick event
     */
   public class MyTask extends TimerTask{
    @Override
    public void run() {
             msg = mHandler.obtainMessage();
            if (bed_status == GAS_CHARGING) {
                msg.what = GAS_CHARGING;
                if (temp >= GAS_MAX) {
                    msg.what = GAS_LIGHT_OFF;
                    temp = GAS_MIN-1;
                }

            }
            else if (bed_status == GAS_RELEASE){
                msg.what = GAS_RELEASE;
                if (temp <= -1){
                    msg.what = GAS_LIGHT_ON;
                    temp = GAS_MAX;
                }

            }
            msg.sendToTarget();
    }
    @Override
    public boolean cancel() {
        return super.cancel();
    }
}

    public void showPressureLevel(int level){

        switch (level){
            case 0:
                setLight(1,true);
                break;
            case 1:
                setLight(2,true);
                break;
            case 2:
                setLight(3,true);
                break;
            case 3 :
                setLight(4,true);
                break;
            case 4:
                setLight(5,true);
                break;
            case 5:
                setLight(6,true);
                break;
            case 6:
                setLight(7,true);
                break;
            default:
                setLight(1,true);
                break;
        }
    }

    /**
     * show pressure level
     * @param level
     * @param light
     */
    public void setLight(int level,boolean light){
        setLightOff();
        for (int i =0;i < level;i++){
            mCheckBoxes[i].setChecked(light);
        }
    }

    /**
     * off all pressure level light
     */
    public void setLightOff(){
        for (int i =0;i < mCheckBoxes.length;i++){
            mCheckBoxes[i].setChecked(false);
        }
    }


    public void setGasChargingLight(){
        temp++;
        mCheckBoxes[temp].setChecked(true);

    }

    public void setGasReleaseLight(){
        mCheckBoxes[temp].setChecked(false);
        temp--;
    }

    public void setGasLightOn(){
        for (int i =0;i < mCheckBoxes.length;i++){
            mCheckBoxes[i].setChecked(true);
        }
    }

}




