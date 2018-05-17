package com.a2013myway.team.capstonedesign;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class ScanActivity extends AppCompatActivity {

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private android.os.Handler mHandler;
    private ListView mListView;
    private BluetoothLeScanner mBLEScanner;

    private AlertDialog dialog;

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;

    private final int REQUEST_ENABLE_BT = 1;
    //Stop scanning after 10 sec
    private static final long SCAN_PERIOD = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mHandler = new android.os.Handler();
        mListView = (ListView)findViewById(R.id.lv_scan);
        dialog = new SpotsDialog(this,R.style.Custom);
        mListView.setEmptyView(findViewById(R.id.emptyview));

        preferences = getSharedPreferences("MacAddress",MODE_PRIVATE);
        editor = preferences.edit();

        //BLE를 지원하는지 확인
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this,"BLE가 지원되지 않는 기종입니다.",Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //블루투스가 지원되는지 확인
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this,"블루투스가 지원되지 않는 기종입니다.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        //BLE 스캐너가 사용가능한지 확인
        if(mBLEScanner==null)
        {
            Toast.makeText(this, "블루투스 LE 스캐너를 사용할 수 없습니다.",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }

        //Initializes list view adapter
        mLeDeviceListAdapter = new ScanActivity.LeDeviceListAdapter();
        mListView.setAdapter(mLeDeviceListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if(device == null)
                    return;
                final Intent intent = new Intent(ScanActivity.this,BluetoothLeService.class);
                intent.putExtra("Name", device.getName());
                intent.putExtra("Address", device.getAddress());
                if(mScanning){
                    mBLEScanner.stopScan(mLeScanCallback);
                }
                String s = preferences.getString("MAC","");
                Log.d("MAC",s);
                editor.remove("MAC").commit();
                s = preferences.getString("MAC","사라짐");
                Log.d("MAC2",s);
                startService(intent);
            }
        });
        scanLeDevice(true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode== Activity.RESULT_OK)
        {
            Toast.makeText(getApplicationContext(),"블루투스가 활성화 되었습니다.",Toast.LENGTH_SHORT).show();
        }
        else if(requestCode==REQUEST_ENABLE_BT&&resultCode==Activity.RESULT_CANCELED)
        {
            Toast.makeText(getApplicationContext(),"앱을 사용하기 위해서는 블루투스 기능이 활성화되어야 합니다.",Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private void scanLeDevice(final boolean enable)
    {
        //Stops scanning after a pre-defined scan period
        dialog.show();
        if(enable)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBLEScanner.stopScan(mLeScanCallback);
                    invalidateOptionsMenu();
                    dialog.dismiss();
                }
            },SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(mLeScanCallback);
        }
        else
        {
            mScanning = false;
            mBLEScanner.stopScan(mLeScanCallback);
        }

        invalidateOptionsMenu();
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflater = getLayoutInflater();
        }

        //디바이스 추가
        public void addDevice(BluetoothDevice device){
            if(!mLeDevices.contains(device)){
                mLeDevices.add(device);
            }
        }

        //리스트 내에 해당 위치에 맞는 디바이스 반환
        public BluetoothDevice getDevice(int position){
            return mLeDevices.get(position);
        }

        public void clear(){
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if(view == null){
                view = mInflater.inflate(R.layout.listitem_device,null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView)view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView)view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else
            {
                viewHolder = (ViewHolder)view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if(deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown Device");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult result : results)
                processResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
        }

        private void processResult(final ScanResult result)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    static class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
    }
}
