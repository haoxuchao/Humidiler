package com.wlp.humidifier;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.wlp.humidifier.adapter.BleAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
  //public final static  String UUID_SERVER="0000ffe0-0000-1000-8000-00805f9b34fb";
 //public final static String UUID="001120a1-2233-4455-6677-889912345678";

public class SearchBtActivity extends AppCompatActivity {
    String A;
    private BluetoothGatt mBluetoothGatt;
    public static final String actionback ="com.example.broadcastback";
    public  static final  UUID read_charact   = UUID.fromString("001120a1-2233-4455-6677-889912345678"); //a1没用来写入数据发送至蓝牙
    public  static final  UUID read_service   = UUID.fromString("001120a0-2233-4455-6677-889912345678");
    public  static final  UUID notify_charact = UUID.fromString("001120a3-2233-4455-6677-889912345678");//用来写指令
    public  static final  UUID notify_service = UUID.fromString("001120a0-2233-4455-6677-889912345678");
   public  static final  UUID write_charact   = UUID.fromString("001120a2-2233-4455-6677-889912345678");
    public  static final  UUID write_service   = UUID.fromString("001120a0-2233-4455-6677-889912345678");
     private static final String TAG ="ble_tag" ;
    ProgressBar pbSearchBle;
    ImageView ivSerBleStatus;
    TextView tvSerBleStatus;
    TextView wenben;
    ListView bleListView;
    private Button btnWrite;
    private Button btnRead;
    private TextView tvResponse;
    private List<BluetoothDevice> mDatas;
    private List<Integer> mRssis;
    private BleAdapter mAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean isScaning=false;
    private boolean isConnecting=false;


    private UUID write_UUID_service;
    private UUID write_UUID_chara;
    private UUID read_UUID_service;
   private UUID read_UUID_chara;
    private UUID notify_UUID_service;
    private UUID notify_UUID_chara;
    private UUID indicate_UUID_service;
    private UUID indicate_UUID_chara;


    /****************************************搜索界面后台运行********************************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent  intetn =new Intent(SearchBtActivity.this,MainActivity.class);
            startActivity(intetn);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"onCreate方法");
        setContentView(R.layout.searchbt);
        initView();
        initData();
        ActivityCollector.addActivity(this);//每启动一个Activity，就将其加入到activity列表中
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        }
        //注册广播
        IntentFilter inf = new IntentFilter(MainActivity.action);
        registerReceiver(recever,inf);
    }
         BroadcastReceiver recever = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 byte [] crash;
                  A = intent.getStringExtra("data");
                  Log.e(TAG,"打印即将发送的指令"+A);
                  crash = HexUtil.hexStringToBytes(A);
                 BluetoothGattService service=mBluetoothGatt.getService(notify_service);
                 BluetoothGattCharacteristic charaWrite=service.getCharacteristic(notify_charact);
                 charaWrite.setValue(crash);
                 mBluetoothGatt.writeCharacteristic(charaWrite);
                 Log.e(TAG, "write"+String.valueOf(crash));

             }
         };

    @Override      //注销广播
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(recever);
        EventBus.getDefault().unregister(this);
    }


    private void initData() {       //实例化
        mDatas=new ArrayList<>();
        mRssis=new ArrayList<>();
        mAdapter=new BleAdapter(SearchBtActivity.this,mDatas,mRssis);
        bleListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }




    private void initView() {             //实例化
        pbSearchBle = findViewById(R.id.progress_ser_bluetooth);
        ivSerBleStatus = findViewById(R.id.iv_ser_ble_status);
        wenben = findViewById(R.id.zhuangtai);
        tvSerBleStatus = findViewById(R.id.tv_ser_ble_status);
        bleListView = findViewById(R.id.ble_list_view);

        ivSerBleStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScaning) {
                    wenben.setText("停止搜索");
                    stopScanDevice();
                } else {
                    checkPermissions();
                }

            }
        });
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScaning) {
                    stopScanDevice();
                }
                if (!isConnecting) {
                    isConnecting = true;
                    BluetoothDevice bluetoothDevice = mDatas.get(position);
                    //连接设备
                    wenben.setText("连接中");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {        //判断运行新API还是旧的API:
                        mBluetoothGatt = bluetoothDevice.connectGatt(SearchBtActivity.this,     //// 包含新API的代码块，安卓6.0系统以上使用该方法
                                true, gattCallback, TRANSPORT_LE);                 //TRANSPORT_LE ：设置传输层模式
                    } else {                                                                      // 包含旧API的代码块
                        mBluetoothGatt = bluetoothDevice.connectGatt(SearchBtActivity.this,
                                true, gattCallback);
                    }
                }

            }
        });
    }
        private BluetoothGattCallback gattCallback=new BluetoothGattCallback()   //返回函数 告知连接是否成功
        {
            /**
             * 断开或连接 状态发生变化时调用
             */
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.e(TAG, "onConnectionStateChange()");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //连接成功
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.e(TAG, "连接成功");
                        //发现服务
                        gatt.discoverServices(); //调用discoverServices方法找到服务
                       // 找到服务算真正连接成功，有两中状态 BluetoothProfiles.State_connected 表示连接  disconneted表断开
                    }
                    if(newState == BluetoothGatt.STATE_DISCONNECTED){
                        Log.e(TAG, "连接已断开");
                        mBluetoothGatt.close();
                    }
                } else {
                    //连接失败
                    Log.e(TAG, "失败==" + status);
                    mBluetoothGatt.close();
                    isConnecting = false;
                }
            }

            /**
             * 发现设备（真正建立连接）
             *///重点
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                isConnecting = false;
                EventBus.getDefault().post("onServicesDiscovered()---建立连接");
                Log.e(TAG, "onServicesDiscovered()---建立连接");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "onServicesDiscovered()---建立连接，发现服务成功");
                    EventBus.getDefault().post("onServicesDiscovered()---建立连接");
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run() {
                        wenben.setText("已连接");
                    }
                });
                gatt.setCharacteristicNotification(mBluetoothGatt.getService(notify_service).getCharacteristic(notify_charact), true);
                List<BluetoothGattDescriptor> descriptorList = mBluetoothGatt.getService(notify_service).getCharacteristic(notify_charact).getDescriptors();
                if (descriptorList != null && descriptorList.size() > 0) {
                    for (BluetoothGattDescriptor descriptor : descriptorList) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }

           @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(read_service).getCharacteristic(read_charact), true);
                List<BluetoothGattDescriptor> descriptorList = mBluetoothGatt.getService(read_service).getCharacteristic(read_charact).getDescriptors();
                if (descriptorList != null && descriptorList.size() > 0) {
                    for (BluetoothGattDescriptor descriptors : descriptorList) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptors);
                    }
                }
            }


          /*
           @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status)
               {
                super.onServicesDiscovered(gatt, status);
                //直到这里才是真正建立了可通信的连接
                isConnecting = false;
                Log.e(TAG, "onServicesDiscovered()---建立连接");
                   if (status == BluetoothGatt.GATT_SUCCESS) {
                       Log.i(TAG, "onServicesDiscovered()---建立连接，发现服务成功");
                   }
                //获取初始化服务和特征值
                  initServiceAndChara();
                //订阅通知
                   mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(notify_service).getCharacteristic(notify_charact), true);
                   mBluetoothGatt.setCharacteristicNotification(mBluetoothGatt.getService(read_service).getCharacteristic(read_charact), true);

                runOnUiThread(new Runnable()
                  {
                    @Override
                    public void run() {
                        wenben.setText("已连接");
                      }
                  });
               }*/

                /**
                 * 读操作的回调
                 * */
                @Override
                public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    /* ; Intent intentback = new Intent(actionback);
                    byte[] bytes = characteristic.getValue();
                    intentback.putExtra("data", bytes);
                    sendBroadcast(intentback)
                    Log.e(TAG, "onCharacteristicChanged " + Arrays.toString(bytes));*/
                        Log.e(TAG, "20190722读取成功" + Arrays.toString(characteristic.getValue()));
                }
                /**
                 * 写操作的回调
                 * */
                @Override
                public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic,int status){
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.e(TAG,"onCharacteristicWrite()  status="+status+",value="+(Arrays.toString(characteristic.getValue())));

                }

                /**
                 * 接收到硬件返回的数据
                 * */
                @Override
                public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                {
                    super.onCharacteristicChanged(gatt, characteristic);
                    final byte[] data=characteristic.getValue();
                    Log.e(TAG, "CharacteristicChange的数据" + data);
                    String B = HexUtil.encodeHexStr(data);
                    Intent intentback = new Intent(actionback);
                    intentback.putExtra("data",B);
                    sendBroadcast(intentback);
                }


        };
           /* private void initServiceAndChara()
            {
                  List<BluetoothGattService> bluetoothGattServices= mBluetoothGatt.getServices();
                   for (BluetoothGattService bluetoothGattService:bluetoothGattServices)
                   {
                 List<BluetoothGattCharacteristic> characteristics=bluetoothGattService.getCharacteristics();
                   for (BluetoothGattCharacteristic characteristic:characteristics){
                    int charaProp = characteristic.getProperties();
             if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    read_UUID_chara=characteristic.getUuid();
                    read_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"read_chara="+read_UUID_chara+"----read_service="+read_UUID_service);
                }
               if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);
                }
             if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);

                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    notify_UUID_chara=characteristic.getUuid();
                    notify_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"notify_chara="+notify_UUID_chara+"----notify_service="+notify_UUID_service);
                }
              if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    indicate_UUID_chara=characteristic.getUuid();
                    indicate_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"indicate_chara="+indicate_UUID_chara+"----indicate_service="+indicate_UUID_service);
                        }
                        }
                    }
            }*/

    /**
     * 开始扫描 10秒后自动停止
     * */
    private void scanDevice(){
        wenben.setText("正在搜索");
        EventBus.getDefault().post("正在搜索");
        isScaning=true;
        pbSearchBle.setVisibility(View.VISIBLE);
        mBluetoothAdapter.startLeScan(scanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //结束扫描
                mBluetoothAdapter.stopLeScan(scanCallback);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isScaning=false;
                        pbSearchBle.setVisibility(View.GONE);
                        wenben.setText("搜索已结束");
                        EventBus.getDefault().post("搜索已结束");
                    }
                });
            }
        },10000);
    }

    /*** 停止扫描* */
    private void stopScanDevice(){
        isScaning=false;
        pbSearchBle.setVisibility(View.GONE);
        mBluetoothAdapter.stopLeScan(scanCallback);
    }

    BluetoothAdapter.LeScanCallback scanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.e(TAG, "run: scanning...");
            if (!mDatas.contains(device)){
                mDatas.add(device);
                mRssis.add(rssi);
                mAdapter.notifyDataSetChanged();
            }

        }
    };

        /*** 检查权限*/
        private void checkPermissions() {
            RxPermissions rxPermissions = new RxPermissions(SearchBtActivity.this);
            rxPermissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(  new io.reactivex.functions.Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                // 用户已经同意该权限
                                scanDevice();
                            } else {
                                // 用户拒绝了该权限，并且选中『不再询问』
                                ToastUtils.showLong("用户开启权限后才能使用");
                            }
                        }
                    });
        }

  /*  @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothGatt.disconnect();   //放到主界面 否则退出连接界面会报错
    }*/
}
