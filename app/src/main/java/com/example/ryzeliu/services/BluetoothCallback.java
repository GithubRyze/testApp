package com.example.ryzeliu.services;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.ryzeliu.bluetoothapp.ControlActivity;
import com.example.ryzeliu.utils.LogUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

/**
 * Created by ryze.liu on 5/18/2016.
 */
public class BluetoothCallback extends BluetoothGattCallback {
    public static UUID SERUUID_STR   = UUID.fromString("00001c00-d102-11e1-9b23-000efb0000a5");

    public static UUID READ_UUID = UUID.fromString("00001c0f-d102-11e1-9b23-000efb0000a5");
    public static UUID WRITE_UUID = UUID.fromString("00001C01-d102-11e1-9b23-000efb0000a5");

    public static UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "BluetoothCallback";
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private Context mContext;
    public static final String CONNECTED_OK = "connected_ok";
    public static final String CONNECTED_FAILURE = "connected_failure";
    public static final String DISCONNECTED = "disconnected";
    public RequestPressureLevel mRequest;
    public ArrayDeque queue = new ArrayDeque(); // save the command FIFO
    public BluetoothCallback(Context context,Handler handler){
            mContext = context;
            mHandler = handler;
    }
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothProfile.STATE_CONNECTED){
            LogUtil.d(TAG, "Connect to gatt server");
            mBluetoothGatt = gatt;
            gatt.discoverServices();
            mHandler.obtainMessage(BluetoothProfile.STATE_CONNECTED, BluetoothProfile.STATE_CONNECTED,-1).sendToTarget();
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED){
            // TODO: 5/24/2016 send message if disconnected
          /*  if (mRequest != null){
                mRequest.destroy();
                mRequest = null;
            }*/
            mHandler.obtainMessage(BluetoothProfile.STATE_DISCONNECTED, BluetoothProfile.STATE_DISCONNECTED,-1).sendToTarget();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
       // LogUtil.d(TAG, "onCharacteristicChanged" + new String(characteristic.getValue()));
        readCharacteric_fromRemoteDevices(characteristic);

    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);

         LogUtil.d(TAG, "onCharacteristicWrite ---->" + status);

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        LogUtil.d(TAG, "onCharacteristicRead ---->" + status);
        }


    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
       // super.onServicesDiscovered(gatt, status);
        LogUtil.d(TAG, "onServicesDiscovered" + status);
        if (status == BluetoothGatt.GATT_SUCCESS){
             setService(gatt);
          //  byte[] requestSide ={(byte)0x97,(byte)0x30};
           // writeCharacteristic(requestSide);
    }
        else{
            LogUtil.d(TAG, "onServicesDiscovered" + status);
        }
    }
    /**
     * add all command in a queue
     * @param write_data
     */
    public void writeCharacteristic(byte[] write_data) {
        if ( mBluetoothGatt == null || write_characteristic == null) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return;
        }
        StringBuilder stringBuilder = null;
       // final byte[] data = characteristic.getValue();
        if (write_data != null && write_data.length > 0) {
            stringBuilder = new StringBuilder(write_data.length);
            for (byte byteChar : write_data)
                stringBuilder.append(String.format("%02X ", byteChar));
        }

        queue.offer(write_data);

    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
//        Log.d(TAG, "readCharacteristic: "+new String(characteristic.getValue()));
        if ( mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter为空");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    private BluetoothGattCharacteristic write_characteristic;
    private BluetoothGattCharacteristic read_characteristic;
    public void setService(final BluetoothGatt gatt){
    List<BluetoothGattService> list = gatt.getServices();
    List<BluetoothGattCharacteristic> mCharacteristics;
    List<BluetoothGattDescriptor> mDescriptors;
    if (list.size()>0){
        for (BluetoothGattService service : list){
            int type = service.getType();
            if (service.getUuid().toString().equalsIgnoreCase(SERUUID_STR.toString())){
                LogUtil.e(TAG, "-->BluetoothGattService uuid:" + service.getUuid());
                LogUtil.e(TAG, "-->BluetoothGattService value:" + service.getType());
                mCharacteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : mCharacteristics){
                    if (characteristic.getUuid().toString().equalsIgnoreCase(WRITE_UUID.toString())){
                        LogUtil.e(TAG, "-->characteristic write uuid:" + characteristic.getUuid());
                        LogUtil.e(TAG, "-->characteristic value:" + characteristic.getValue());
                        LogUtil.e(TAG, "-->characteristic getProperties:" + characteristic.getProperties());
                        LogUtil.e(TAG, "-->characteristic getPermissions:" + characteristic.getPermissions());
                        write_characteristic = characteristic;
                        setCharacteristicNotification(write_characteristic,true);
                     }
                 else if(characteristic.getUuid().toString().equalsIgnoreCase(READ_UUID.toString())){
                        LogUtil.e(TAG, "-->characteristic read uuid:" + characteristic.getUuid());
                        LogUtil.e(TAG, "-->characteristic value:" + characteristic.getValue());
                        LogUtil.e(TAG, "-->characteristic getProperties:" + characteristic.getProperties());
                        LogUtil.e(TAG, "-->characteristic getPermissions:" + characteristic.getPermissions());
                        read_characteristic = characteristic;
                        setCharacteristicNotification(read_characteristic,true);
                        readCharacteristic(read_characteristic);
                    }
                    mDescriptors = characteristic.getDescriptors();
                    for (BluetoothGattDescriptor  descriptor : mDescriptors){
                        LogUtil.e(TAG, "-->BluetoothGattDescriptor  uuid:" + descriptor.getUuid());
                        LogUtil.e(TAG, "-->BluetoothGattDescriptor value:" + descriptor.getValue());
                        LogUtil.e(TAG, "-->BluetoothGattDescriptor getPermissions:" + descriptor.getPermissions());
                    }
                }
            }
        }
        }
        mRequest = new RequestPressureLevel();
        mRequest.start();
        queueThread.start();
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    private void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothGatt == null) {
            Log.d(TAG, "mBluetoothGatt not connected");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void readCharacteric_fromRemoteDevices(BluetoothGattCharacteristic characteristic) {

            // For all other profiles, writes the data formatted in HEX.
            StringBuilder stringBuilder = null;
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
            }
            String level = stringBuilder.toString();
            if (level.length() == 0){
            LogUtil.e(TAG,"pressure level  value is null");
                return;
        }
            LogUtil.e(TAG,level);
             parseCharacteristicInfo(level);
        }

        public void closeConnected(){
            if (mBluetoothGatt == null)
                return;
            mBluetoothGatt.close();
            if (mRequest != null) {
                mRequest.cancel();
                mRequest = null;
            }
            if (queueThread != null){
                queueThread = null;
            }
            mBluetoothGatt= null;
        }

        public void disconnected() {
            if (mBluetoothGatt == null) {
                LogUtil.e(TAG, "BluetoothGatt not initialized");
                return;
            }
            mBluetoothGatt.disconnect();
        }
    //parse the remote devices data
    private static final String LEVEL_1 = "79 40 ";
    private static final String LEVEL_2 = "79 41 ";
    private static final String LEVEL_3 = "79 42 ";
    private static final String LEVEL_4 = "79 43 ";
    private static final String LEVEL_5 = "79 44 ";
    private static final String LEVEL_6 = "79 45 ";
    private static final String LEVEL_7 = "79 46 ";
    private static final String LEFT_SIDE ="79 10 ";
    private static final String RIGHT_SIDE = "79 11 ";
    private int temLevel;
        private void parseCharacteristicInfo(String level){
            int pressure_level;
            Message msg = mHandler.obtainMessage(ControlActivity.GET_PRESSURE_LEVEL);
            Bundle bundle = new Bundle();
              if (level.equals(LEVEL_1)) {
                  pressure_level = 0;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_2)){
                  pressure_level = 1;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_3)){
                  pressure_level = 2;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_4)){
                  pressure_level = 3;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_5)){
                  pressure_level = 4;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_6)){
                  pressure_level = 5;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }else if (level.equals(LEVEL_7)){
                  pressure_level = 6;
                  bundle.putInt(ControlActivity.LEVEL_KEY, pressure_level);
              }
              else{
                  return;
              }
               //if (temLevel != pressure_level) {
                    temLevel = pressure_level;
                    msg.setData(bundle);
                    msg.sendToTarget();
               //}
            }

    /**
     * request remote devices pressure value every 3s
     */
     private class RequestPressureLevel extends Thread{
        byte[] requestPressure = {(byte)0x97,(byte)0x32};
         private  boolean run;
         public boolean getRun(){
             return run;
         }
         public void setRun(boolean mRun){
             this.run = mRun;
         }
         public RequestPressureLevel(){
                    run = true;
         }

         @Override
         public void run() {

            while (run){
                try {
                    sleep(3000);
                    writeCharacteristic(requestPressure);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
         }

         }

         public void cancel() {
             LogUtil.d(TAG,"cancel request the pressure level value");
             setRun(false);
         }
     }

    /**
     * send characteristic to remote device
     */

        Thread queueThread = new Thread(){

            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(300);
                        if(!queue.isEmpty()) {
                            if (mBluetoothGatt != null) {
                                write_characteristic.setValue((byte[]) queue.poll());
                                //if writeCharacteristic not success
                                if (!mBluetoothGatt.writeCharacteristic(write_characteristic)){
                                    queue.addFirst(write_characteristic.getValue());
                                }

                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };


    }


