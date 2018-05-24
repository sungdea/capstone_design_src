//package com.a2013myway.team.capstonedesign;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanResult;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.os.AsyncTask;
//import android.os.IBinder;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import dmax.dialog.SpotsDialog;
//
//public class ScanActivity extends AppCompatActivity {
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scan);
//
//
//
//        //BLE를 지원하는지 확인
//        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
//        {
//            Toast.makeText(this,"BLE가 지원되지 않는 기종입니다.",Toast.LENGTH_SHORT).show();
//            finish();
//        }
//

//
//        //블루투스가 지원되는지 확인
//        if(mBluetoothAdapter == null)
//        {
//            Toast.makeText(this,"블루투스가 지원되지 않는 기종입니다.",Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
//        //BLE 스캐너가 사용가능한지 확인
//        if(mBLEScanner==null)
//        {
//            Toast.makeText(this, "블루투스 LE 스캐너를 사용할 수 없습니다.",Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(!mBluetoothAdapter.isEnabled()){
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
//        }
//
//        //Initializes list view adapter
//
//        scanLeDevice(true);
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == REQUEST_ENABLE_BT && resultCode== Activity.RESULT_OK)
//        {
//            Toast.makeText(getApplicationContext(),"블루투스가 활성화 되었습니다.",Toast.LENGTH_SHORT).show();
//        }
//        else if(requestCode==REQUEST_ENABLE_BT&&resultCode==Activity.RESULT_CANCELED)
//        {
//            Toast.makeText(getApplicationContext(),"앱을 사용하기 위해서는 블루투스 기능이 활성화되어야 합니다.",Toast.LENGTH_SHORT).show();
//            finish();
//        }
//        super.onActivityResult(requestCode,resultCode,data);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        scanLeDevice(false);
//        mLeDeviceListAdapter.clear();
//    }
//
//
//
//}
