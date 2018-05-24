package com.a2013myway.team.capstonedesign;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
    private DataInfoTTS Dit;

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
        Dit=new DataInfoTTS(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle("Capstone Design");
        builder.setContentText("태그 감지 상태 입니다.");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        Notification notification = builder.build();

        startForeground(1,notification);

        //MAC주소 불러오기
        preferences = getSharedPreferences("MacAddress",MODE_PRIVATE);
        editor = preferences.edit();

        super.onCreate();
    }

    //연결 여부에 따라 broadcast전송
    private final BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                tts.speak("블루투스 연결에 성공하였습니다.");

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isdiscover = gatt.discoverServices();
                        Log.d("서비스 발견",isdiscover+"");
                    }
                },600);
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                tts.speak("블루투스 연결이 해제되었습니다.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d("onservicediscoverd","진입");
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothGattService service = gatt.getService(SERVICE_UUID);

                if(service == null)
                {
                    Log.d("service","서비스가 발견되지 않았습니다.");
                    tts.speak("잘못된 블루투스 장치와 연결되어 연결을 해제합니다.");
                    disconnect();
                    return;
                }

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_UUID);

                gatt.setCharacteristicNotification(characteristic,true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCC);

                //보드에서 데이터를 보낼 시 NOTIFICATION을 받을 수 있도록 DESCRIPTOR에 속성 지정
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
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
        byte[] data = characteristic.getValue();
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        Intent intent = new Intent(action);

        //Byte to ASCII
        if (data != null && data.length > 0) {
            //맨뒤 널값 두개 지우기 위해서 length - 1
            for (int i = 0; i<data.length-1; i++)
                stringBuilder.append((char)(data[i]));

            Log.d("tagnum",stringBuilder.toString());

            Dit.run(stringBuilder.toString());

            intent.putExtra("DATA",stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

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
        close();
    }

    //ble 디바이스를 이용한 후 app must call this method to ensure resources are released properly
    public void close(){
        if(mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
}