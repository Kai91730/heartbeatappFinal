package com.example.user.heartbeatapp;


import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceList extends Activity {
    // 已配對到的裝置/搜索到的裝置
    private TextView tvDeviceListPairedDeviceTitle, tvDeviceListNewDeviceTitle;
    private ListView lvDeviceListPairedDevice, lvDeviceListNewDevice;
    //搜尋裝置按鈕
    private Button btnDeviceListScan;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    public static String DEVICE_ADDRESS = "deviceAddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //搜尋裝置時的進度條
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        setResult(Activity.RESULT_CANCELED);

        getWidgetReferences();
        bindEventHandler();
        initializeValues();
    }
    //定義各元件的名稱
    private void getWidgetReferences() {
        tvDeviceListPairedDeviceTitle = (TextView) findViewById(R.id.tvDeviceListPairedDeviceTitle);
        tvDeviceListNewDeviceTitle = (TextView) findViewById(R.id.tvDeviceListNewDeviceTitle);

        lvDeviceListPairedDevice = (ListView) findViewById(R.id.lvDeviceListPairedDevice);
        lvDeviceListNewDevice = (ListView) findViewById(R.id.lvDeviceListNewDevice);

        btnDeviceListScan = (Button) findViewById(R.id.btnDeviceListScan);

    }
    //設置監聽器
    private void bindEventHandler() {
        lvDeviceListPairedDevice.setOnItemClickListener(mDeviceClickListener);
        lvDeviceListNewDevice.setOnItemClickListener(mDeviceClickListener);

        btnDeviceListScan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startDiscovery();
                btnDeviceListScan.setVisibility(View.GONE);
            }
        });
    }
    //初始化數值
    private void initializeValues() {
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);
        //設定Adapter
        lvDeviceListPairedDevice.setAdapter(pairedDevicesArrayAdapter);
        lvDeviceListNewDevice.setAdapter(newDevicesArrayAdapter);

        //  將發現裝置的訊息送給廣播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        //  將裝置搜尋完成的訊息送給廣播
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        //回傳已配對到的裝置給Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
                .getBondedDevices();

        // 將已配對的裝置加入到陣列
        if (pairedDevices.size() > 0) {
            tvDeviceListPairedDeviceTitle.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n"
                        + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired)
                    .toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
    }
    //開始搜尋
    private void startDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        tvDeviceListNewDeviceTitle.setVisibility(View.VISIBLE);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    //ListView的點擊事件
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    //建立廣播接收元件物件
    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接收廣播
            String action = intent.getAction();
            //
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //添加已配對的裝置
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    newDevicesArrayAdapter.add(device.getName() + "\n"
                            + device.getAddress());
                }
            }
            //搜尋完成時
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                //沒有找到裝置
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(
                            R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
        //移除廣播
        this.unregisterReceiver(discoveryFinishReceiver);
    }

}
