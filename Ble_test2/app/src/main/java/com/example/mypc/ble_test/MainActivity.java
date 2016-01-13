package com.example.mypc.ble_test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener{
    private final static String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeClass mBLE = null;
    private List<Fruit> fruitList = new ArrayList<Fruit>();
    Button mBut_send;
    TextView mTextViex;
    ViewPager mViewPager;

    String Data_RX = null;

    private final static String UUID_KEY_DATA = "0000ffe9-0000-1000-8000-00805f9b34fb";
    private final static String UUID_KEY_DATA2 = "0000ffe4-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFruits();
        FruitAdapter adapter = new FruitAdapter(MainActivity.this,
                R.layout.fruit_item, fruitList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Fruit fruit = fruitList.get(position);
                Toast.makeText(MainActivity.this, fruit.getName(),
                        Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                BleConect(fruit.getAddress());
            }
        });
        mBut_send = (Button)findViewById(R.id.but_send);
        mBut_send.setOnClickListener(this);
        mTextViex = (TextView)findViewById(R.id.text_data);
        mTextViex.setMovementMethod(new ScrollingMovementMethod());
        mTextViex.setText("接受数据在这里显示");
        mViewPager = (ViewPager)findViewById(R.id.viwe_show);
        //检查是否支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不指示蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //开启蓝牙
        mBluetoothAdapter.enable();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            //	my=this;
            Toast.makeText(this, "找不到", Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        timerTask(); // 定时执行
    }
    int num_count = 0;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if(num_count < 200) {
                        num_count++;
                    } else {
                        num_count = 0;
                        mTextViex.setText(null);
                    }
                    //完成主界面更新,拿到数据
                    String data = (String)msg.obj;
                    mTextViex.setText(mTextViex.getText()+Integer.toString(num_count)+"--"+data+"\n");
                    break;
                case 1:
                    // 处理具体的逻辑
                    if(num_count < 15) {
                        num_count++;
                    } else {
                        num_count = 0;
                        mTextViex.setText(null);
                    }
                    //完成主界面更新,拿到数据
                    data = "凌海滨";
                    mTextViex.setText(mTextViex.getText()+Integer.toString(num_count)+"--"+data+"\n");
                    if(num_count > 13) {
                        mViewPager.setBackgroundResource(R.drawable.daku);
                    } else if(num_count > 10) {
                        mViewPager.setBackgroundResource(R.drawable.daxiao);
                    } else if(num_count > 8) {
                        mViewPager.setBackgroundResource(R.drawable.ku);
                    } else if(num_count > 6) {
                        mViewPager.setBackgroundResource(R.drawable.qingyixia);
                    } else if(num_count > 4) {
                        mViewPager.setBackgroundResource(R.drawable.wuzuixiao);
                    } else {
                        mViewPager.setBackgroundResource(R.drawable.wunai);
                    }
                    break;
                default:
                    break;
            }
        }

    };

    public Timer mTimer = new Timer();// 定时器
    private int count = 1;
    public void timerTask() {
        //创建定时线程执行更新任务
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(count<=50){
                    System.out.println("TimerTask-->Id is "
                            + Thread.currentThread().getId());// TimerTask在它自己的线程中
                    mHandler.sendEmptyMessage(1);// 向Handler发送消息
                }else{
                    mHandler.sendEmptyMessage(1);// 向Handler发送消息停止继续执行
                }
                count++;
            }
        }, 3000, 200);// 定时任务
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_send:
                byte[] tmpa = {0x3a,0x08,0x6f,0x6f,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0x0a};
               // String xx = mEditText.getText().toString();
                //byte[] midbytes=xx.getBytes();
                //tmpa = hexStr2Bytes(xx);
                writeCommand(tmpa);
                break;
            default:break;
        }
    }
    private void initFruits() {
        //Fruit apple = new Fruit("Apple",    R.mipmap.ic_launcher);
        //fruitList.add(apple);
        //Fruit banana = new Fruit("Banana", R.mipmap.ic_launcher);
        //fruitList.add(banana);
    }

    /**
     * bytes字符串转换为Byte值
     * @param String src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src)
    {
        int m=0,n=0;
        int l=src.length()/2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
        }
        return ret;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    Fruit apple = new Fruit(device.getName(), device.toString(), R.drawable.daxiao);
                    fruitList.add(apple);
                    FruitAdapter adapter = new FruitAdapter(MainActivity.this,
                            R.layout.fruit_item, fruitList);
                    ListView listView = (ListView) findViewById(R.id.list_view);
                    listView.setAdapter(adapter);
                    //mLeDeviceListAdapter.addDevice(device);
                    //mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public void BleConect(String address) {
        mBLE = new BluetoothLeClass(this);
        if (!mBLE.initialize()) {
            finish();
        }
        // 发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        // 收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        mBLE.connect(address);
    }

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeClass.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }

    };

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new BluetoothLeClass.OnDataAvailableListener() {

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(MainActivity.this, "被读", Toast.LENGTH_SHORT).show();
            }
        }
        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            byte[] ble_data = null;
            ble_data = characteristic.getValue();
            if(ble_data.equals("byte[13]@3806")) {
                Log.e(TAG, "---->服务来了");
            } else {
                Log.e(TAG, "---->凌海滨:" + new String(ble_data));
                Log.e(TAG, "---->凌海滨:" + ble_data);
                Data_RX = new String(ble_data);
                Data_RX = byte2HexStr(ble_data);
                Log.e(TAG, "---->凌海滨:" + Data_RX);
                //耗时操作，完成之后发送消息给Handler，完成UI更新；
               // mHandler.sendEmptyMessage(0);
                //需要数据传递，用下面方法；
                Message msg =new Message();
                msg.what = 0;
                msg.obj = Data_RX;//可以是基本类型，可以是对象，可以是List、map等；
                mHandler.sendMessage(msg);
            }
           // Toast.makeText(MainActivity.this, "有数据", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * bytes转换成十六进制字符串
     * @param byte[] b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        /**
         * 销毁定时器的方式
         */
        mTimer.cancel();// 程序退出时cancel timer
        mBLE.disconnect();
        mBLE.close();
    }

    public void writeCommand(byte[] comm) {
        // if(m_gattServices==null)return;
        List<BluetoothGattService> gattServices = mBLE
                .getSupportedGattServices();
        if (gattServices == null)
            return;
        // for(BluetoothGattService gattService : m_gattServices) {
        for (BluetoothGattService gattService : gattServices) {
            // BluetoothGattService gattService=m_gattServices.get(i);
            // -----Service的字段信息-----//
            int type = gattService.getType();
            // Log.e(TAG,"-->service type:"+Utils.getServiceType(type));
            // Log.e(TAG,"-->includedServices size:"+gattService.getIncludedServices().size());
            // Log.e(TAG,"-->service uuid:"+gattService.getUuid());

            // -----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                // Log.e(TAG,"---->char uuid:"+gattCharacteristic.getUuid());

                // Toast.makeText(this,
                // "---->char uuid:"+gattCharacteristic.getUuid(),
                // Toast.LENGTH_SHORT).show();

                int permission = gattCharacteristic.getPermissions();
                // Log.e(TAG,"---->权限:"+Utils.getCharPermission(permission));
                // Toast.makeText(this,
                // "---->权限:"+Utils.getCharPermission(permission),
                // Toast.LENGTH_SHORT).show();

                int property = gattCharacteristic.getProperties();
                // Log.e(TAG,"---->属性:"+Utils.getCharPropertie(property));
                // Toast.makeText(this,
                // "---->属性:"+Utils.getCharPropertie(property),
                // Toast.LENGTH_SHORT).show();

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    //Log.e(TAG, "---->char value:" + new String(data));
                }
                byte[] jias = comm;
                String senddata = new String(jias);
                gattCharacteristic.setValue(senddata);
                // 往蓝牙模块写入数据
                // mBLE.writeCharacteristic(gattCharacteristic);
                // UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
                    try {
                        BluetoothGattService bluetoothGattService = mBLE.mBluetoothGatt
                                .getService(gattCharacteristic.getUuid()); // 获取UUID对应的服务。

                        BluetoothGattCharacteristic charac = bluetoothGattService
                                .getCharacteristic(gattCharacteristic.getUuid()); // 获取指定服务下的特性
                    } catch (Exception e) {

                    }

                    // 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    // mHandler.postDelayed(new Runnable() {
                    // @Override
                    // public void run() {
                    mBLE.readCharacteristic(gattCharacteristic);
                    // }
                    // }, 500);

                    // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    // 设置数据内容
                    // byte[] jias={0x2b,0x30,0x07};
                    // String senddata=new String(jias);
                    gattCharacteristic.setValue(senddata);
                    // 往蓝牙模块写入数据
                    mBLE.writeCharacteristic(gattCharacteristic);
                }
                if (gattCharacteristic.getUuid().toString()
                        .equals(UUID_KEY_DATA2)) {
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                }
            }
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        for (BluetoothGattService gattService : gattServices) {
            // -----Service的字段信息-----//
            int type = gattService.getType();
           // Log.e(TAG, "-->service type:" + Utils.getServiceType(type));
            // Ev2.setText("-->service type:"+Utils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:"
                    + gattService.getIncludedServices().size());
            // Ev2.setText("-->includedServices size:"+gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());
            // Ev2.setText("-->service uuid:"+gattService.getUuid());
            // -----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());
                // Ev2.setText("---->char uuid:"+gattCharacteristic.getUuid());
                int permission = gattCharacteristic.getPermissions();
                // Ev2.setText("---->char permission:"+Utils.getCharPermission(permission));
                int property = gattCharacteristic.getProperties();
                // Ev2.setText("---->char property:"+Utils.getCharPropertie(property));
                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                    Toast.makeText(MainActivity.this, "有数据", Toast.LENGTH_SHORT).show();
                    // Ev2.setText("---->char value:"+new String(data));
                }
                // UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if (gattCharacteristic.getUuid().toString()
                        .equals(UUID_KEY_DATA2)) {
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                }
            }
        }
    }
}