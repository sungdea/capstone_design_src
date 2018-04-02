package com.a2013myway.team.capstonedesign;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by shotc on 2018-03-25.
 */

public class DeviceScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private android.os.Handler mHandler;

    private BluetoothLeScanner mBLEScanner;

    private final int REQUEST_ENABLE_BT = 1;
    //Stop scanning after 10 sec
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new android.os.Handler();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }

        //Initializes list view adapter
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if(device == null)
            return;

        final Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if(mScanning){
            mBLEScanner.stopScan(mLeScanCallback);
        }
    }

    private void scanLeDevice(final boolean enable)
    {
        //Stops scanning after a pre-defined scan period
        if(enable)
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBLEScanner.stopScan(mLeScanCallback);
                    invalidateOptionsMenu();
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

    private class LeDeviceListAdapter extends BaseAdapter{
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflater;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflater = DeviceScanActivity.this.getLayoutInflater();
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
            //Log.d("tag","스캔 진입");
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
