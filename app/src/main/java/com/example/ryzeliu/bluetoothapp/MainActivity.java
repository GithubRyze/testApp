package com.example.ryzeliu.bluetoothapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ryzeliu.utils.LogUtil;
import com.example.ryzeliu.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager manager;
    public static final String DEVICE = "DEVICES";
    public static final int MESSAGE_WRITE_DATE =1;
    public static final int MESSAGE_READ_DATE = 2;
    private static final int REQUEST_ENABLE_BL = 3;
    public static final int MESSAGE_CONNECTION_FAILURE = 5;
    public static final int MESSAGE_CONNECTION_OK = 4;
    public static final int REQUEST_SCAN_BLE = 6;
    private ListView lt_deviceList;
    public static final String CONNECTION_FAILURE = "CON_FAILURE";
    public static final String MESSAGE_DEVICE_NAME = "DEVICES_NAME";
    public static final String SLAVE_DEVICE = "DEVICE";
   // private BluetoothLeScanner mBluetoothLeScanner;
    private ArrayList<BluetoothDevice> mArrayList;
    private DeviceAdapter mDeviceAdapter;
    private ProgressBar scan_bar;
    private Handler mHandle;
    private boolean mScanning = false;
    private UUID uuid = UUID.fromString(Utils.BL_UUID);
    private UUID[] filter = {uuid};
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArrayList = new ArrayList<>();
        mDeviceAdapter = new DeviceAdapter(this,mArrayList);
        mHandle = new Handler();
        manager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
       // mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        initView();
        if (mBluetoothAdapter == null){
            Toast.makeText(this,R.string.bluetooth_unavailable,Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (mayRequestLocation()) {
            scanLeDevice(true);
        }
    }

    private void initView(){
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        scan_bar = (ProgressBar) findViewById(R.id.scan_bar);
        lt_deviceList = (ListView) findViewById(R.id.lt_deviceList);
        lt_deviceList.setOnItemClickListener(mOnItemClick);
        lt_deviceList.setAdapter(mDeviceAdapter);

    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BL);
        }
        super.onResume();

    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scan_bar.setVisibility(View.GONE);

                        mBluetoothAdapter.stopLeScan(mLeScanCallback);


                    mScanning = false;
                }
            }, 20000);
          if (!mScanning) {
              LogUtil.d(TAG, "start scan le device");
              mScanning = true;
              mBluetoothAdapter.startLeScan(mLeScanCallback);
              scan_bar.setVisibility(View.VISIBLE);
          }
        }
        else{
            if (mScanning) {
                LogUtil.d(TAG, "stop scan le device");
                mScanning = false;
                scan_bar.setVisibility(View.GONE);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);


            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BL) {
            if (resultCode == Activity.RESULT_OK) {
                scanLeDevice(true);
            }
            else{
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        scanLeDevice(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        scanLeDevice(false);
        super.onDestroy();
    }
    final int REQUEST_FINE_LOCATION = 1;
    private String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            LogUtil.d(TAG,"REQUEST PERMISSION");
            requestPermissions(new String[]{permission}, REQUEST_FINE_LOCATION);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            if (requestCode == 1){
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    scanLeDevice(true);
                } else {
                    this.finish();
                }
            }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_scan:
            case R.id.menu_refresh:
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener mOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   BluetoothDevice device = (BluetoothDevice) mDeviceAdapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this,ControlActivity.class);
                    intent.putExtra(DEVICE,device);
                    startActivity(intent);
        }
    };
    private ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, result.getDevice().getAddress() + " ScanCallback:::" + result.getDevice().getName());
                    if (!mArrayList.contains(result.getDevice())) {
                        mArrayList.add(result.getDevice());
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtil.d(TAG, device.getAddress()+" LeScanCallback:::"+device.getName());
                            if (!mArrayList.contains(device)) {
                                mArrayList.add(device);
                                mDeviceAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };

    private class DeviceAdapter extends BaseAdapter{
        private ArrayList<BluetoothDevice> mDevices;
        private Context mContext;
        private BluetoothDevice device;
        public DeviceAdapter(Context context,ArrayList<BluetoothDevice> list){
                this.mDevices = list;
                this.mContext = context;
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            convertView = View.inflate(mContext,R.layout.devices_adapter,null);
            TextView tv_deviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            TextView tv_deviceAddress = (TextView) convertView.findViewById(R.id.tv_device_address);
            device = mDevices.get(position);
            tv_deviceName.setText(device.getName());
            tv_deviceAddress.setText(device.getAddress());
            return convertView;
        }

    }
}
