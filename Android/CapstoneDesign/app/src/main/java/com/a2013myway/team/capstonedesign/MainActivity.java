/*
18.03.17
modified by LSH
 */

package com.a2013myway.team.capstonedesign;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;


/*
USE
TextToSpeech 객체를 생성하고 밑과 같이 동적할당 해준 뒤에
speak 메서드를 이용하면 된다.
 */

public class MainActivity extends AppCompatActivity {

    //PERMISSION REQUEST CODE
    private static final int PERMISSION_REQUEST = 1;

    private BluetoothAdapter bluetoothAdapter;
    private final int ENABLE_BT = 1;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private android.os.Handler mHandler;
    private ListView mListView;
    private BluetoothLeScanner mBLEScanner;
    private BroadcastReceiver broadcastReceiver;
    private AlertDialog dialog;
    public final static String ACTION_GATT_CONNECTED =
            "com.a2013myway.team.capstonedesign.ACTION_GATT_CONNECTED";
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
    };;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;

    private final int REQUEST_ENABLE_BT = 1;
    //Stop scanning after 10 sec
    private static final long SCAN_PERIOD = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new android.os.Handler();
        mListView = (ListView)findViewById(R.id.lv_scanlist);
        dialog = new SpotsDialog(this,R.style.Custom);
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,ENABLE_BT);
        }
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mListView.setAdapter(mLeDeviceListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if(device == null)
                    return;
                final Intent intent = new Intent(MainActivity.this,BluetoothLeService.class);
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


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    if(intent.getAction() == ACTION_GATT_CONNECTED)
                        finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_GATT_CONNECTED));
        ImageButton button = findViewById(R.id.btn_search);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanLeDevice(true);
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setVisibility(View.INVISIBLE);
            }
        });



        //PERMISSION CHECK
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("권한이 필요합니다.").setMessage("앱을 이용하기 위해서 권한에 동의해주세요.").setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST);
                    }
                });
                builder.show();
            }
        }


//        mListView.setEmptyView(findViewById(R.id.emptyview));

        preferences = getSharedPreferences("MacAddress",MODE_PRIVATE);
        editor = preferences.edit();
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




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST)
        {
            if(grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(getApplicationContext(),"권한에 동의해주시지 않으면 원활한 앱 실행이 불가능합니다.",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_BT && resultCode== Activity.RESULT_OK)
        {
            Toast.makeText(getApplicationContext(),"블루투스가 활성화 되었습니다.",Toast.LENGTH_SHORT).show();
        }
        else if(requestCode==ENABLE_BT&&resultCode==Activity.RESULT_CANCELED)
        {
            Toast.makeText(getApplicationContext(),"앱을 사용하기 위해서는 블루투스 기능이 활성화되어야 합니다.",Toast.LENGTH_SHORT).show();
            finish();
        }
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


    static class ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
    }
}
