package com.a2013myway.team.capstonedesign;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private UUID SERVICE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700");
    private UUID DATA_UUID = UUID.fromString("7772E5DB-3868-4112-A1A9-F2669D106BF3");
    //Client Characteristic Configuration
    private UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

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

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DeviceName = intent.getStringExtra("Name");
        DeviceAddress = intent.getStringExtra("Address");
        initialize();
        tts = new TTS(getApplicationContext(), Locale.KOREAN);
        boolean isconnect = false;

        String savedMacAddress = preferences.getString("MAC",null);
        if(savedMacAddress == null)
        {
            isconnect = connect(DeviceAddress);
        }
        else
        {
            Log.d("진입","진입성공");
            isconnect = connect(savedMacAddress);
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

        Notification notification = builder.build();

        startForeground(1,notification);

        preferences = getSharedPreferences("MacAddress",MODE_PRIVATE);
        editor = preferences.edit();

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
                gatt.getServices();

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

                //uuid 구현할것
                //gatt.getServices를 통해 이 메소드로 넘어오는 것 대신 위에서 getService로 바로 uuid접근을 해도 되는 것인지
                //1개의 서비스만을 정의하여 굳이 uuid 없이 services로 불러온 후 사용할 수 잇는지 확인
                BluetoothGattService service = gatt.getService(SERVICE_UUID);

                if(service == null)
                {
                    Log.d("service","서비스가 발견되지 않았습니다.");
                    return;
                }

                //service uuid만 정하고 get 할 수 있는지 확인
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_UUID);

                gatt.setCharacteristicNotification(characteristic,true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCC);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

                Log.d("tagnum",stringBuilder.toString());
                Toast.makeText(getApplicationContext(),stringBuilder.toString(),Toast.LENGTH_SHORT).show();
                TTS tts = new TTS(getApplicationContext(),Locale.KOREAN);
                tts.speak(stringBuilder.toString());
            }

        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);

            readCharacteristic(characteristic);
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
    public boolean connect(final String address){
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

        mBluetoothGatt = device.connectGatt(this,true,mGattCallBack);
        mBluetoothDeviceAddress = address;

        editor.putString("MAC",mBluetoothDeviceAddress);
        editor.commit();
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
