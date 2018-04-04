package com.a2013myway.team.capstonedesign;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BluetoothLeService extends Service {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;


    private TTS tts;

    private  static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private String DeviceName;
    private String DeviceAddress = null;

    public final static String ACTION_GATT_CONNECTED =
            "com.a2013myway.team.capstonedesign.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.a2013myway.team.capstonedesign.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.a2013myway.team.capstonedesign.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.a2013myway.team.capstonedesign.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.a2013myway.team.capstonedesign.EXTRA_DATA";

    //public final static UUID


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DeviceName = intent.getStringExtra("Name");
        DeviceAddress = intent.getStringExtra("Address");
        initialize();
        tts = new TTS(getApplicationContext(), Locale.KOREAN);
        boolean isconnect = false;

        Log.d("onstartcommand 진입","진입");

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices

        Log.d("bondnum",pairedDevices.size()+"");

        if(pairedDevices.size()>0)
        {
            for(BluetoothDevice device : pairedDevices){ //페어링된 장치 이름과, MAC주소를 가져올 수 있다.
                if(device.getAddress().equals(DeviceAddress))
                {
                    isconnect = connect(device.getAddress(),false);
                    break;
                }
                else
                {
                    isconnect = connect(DeviceAddress,true);
                }
            }
        }
        else{
            //본딩된 기기가 없는 경우 연결 시 본딩할 수 있도록 함
            isconnect = connect(DeviceAddress,true);
        }



        Log.d("isconnect",isconnect+"");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Capstone Design");
        builder.setContentText("태그 감지 상태 입니다.");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        super.onCreate();
    }

    //연결 여부에 따라 broadcast전송
    private final BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                tts.speak("블루투스 연결에 성공하였습니다.");
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                tts.speak("블루투스 연결이 해제되었습니다.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };

    private void broadcastUpdate(final String action){
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    //프로파일 handling
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
    {

    }

    public class LocalBinder extends Binder{
        BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }

    public BluetoothLeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();


    //블루투스 어댑터와 블루투스 매니저 할당
    public boolean initialize(){
        if(mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null)
            {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter==null){
            return false;
        }

        return true;
    }

    //connects to the gatt server hosted on the bluetooth le device
    public boolean connect(final String address, boolean bond){
        if(mBluetoothAdapter == null || address == null)
        {
            return false;
        }
        //이전에 연결되었던 디바이스, 연결을 재시도 함.
        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null){
            if(mBluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return true;
            }else
            {
                return false;
            }
        }

        //해당 주소에 맞는 device를 가져옴
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null)
        {
            return false;
        }

        if(bond) {
            boolean isbond = device.createBond();
        }
        mBluetoothGatt = device.connectGatt(this,true,mGattCallBack);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    //커넥션 중지
    public void disconnect(){
        if (mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    //ble 디바이스를 이용한 후 app must call this method to ensure resources are released properly
    public void close(){
        if(mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if(mBluetoothAdapter == null || mBluetoothGatt == null)
        {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //enable or disables notification on a give characteristic
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if(mBluetoothAdapter == null || mBluetoothGatt == null)
            return;

        //specific
    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if(mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }




}
