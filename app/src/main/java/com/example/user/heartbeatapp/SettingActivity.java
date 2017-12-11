package com.example.user.heartbeatapp;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.os.Build;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.widget.LinearLayout;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private static final String TAG = "SettingActivity";

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;

    private ListView lvMainChat;

    private XYSeries series = null;
    private XYMultipleSeriesDataset mDataset;
    private GraphicalView chart;
    private XYMultipleSeriesRenderer renderer;
    private int yMax = 20;//y軸最大值，根據不同傳感器變化
    private int xMax = 50;//顯示測量次數
    private int yMin = 0;
    public int BPM = 0;

    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;

    BluetoothAdapter mBluetoothAdapter = null;
    private ChatService chatService = null;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            //    狀態設置為("Connected to device");
                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            //     狀態設置為("Connecting...");
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            //   狀態設置為("not_connected");
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    //接收Arduino的值
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //將英文去除留下數字
                    readMessage = readMessage.replaceAll("[^-?0-9]+", "");
                    //有訊息則收值
                    if(!readMessage.isEmpty()){
                        chatArrayAdapter.add("BPM:  " + readMessage);
                        //將字串轉為數字並存進BPM裡
                        try {
                            BPM = Integer.parseInt(readMessage);
                        } catch (NumberFormatException e) {
                        }
                        //更新圖表
                        updateChart();
                    }
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    //程式一開始執行時的函式
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //mBTDevices = new ArrayList<>();
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        //開啟區域藍芽接口
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //初始化圖表
        initChart("Times(测量次数)", "123",0,xMax,yMin,yMax);

        //新增並初始化ListView
        getWidgetReferences();
        lvMainChat.setOnItemClickListener(SettingActivity.this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart: called.");

        //檢查藍芽是否開啟
        if (!mBluetoothAdapter.isEnabled()) {
            //要求開啟藍芽
            Intent enableIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            //確定chatservice是否執行中
            if (chatService == null)
                setupChat();
        }
    }

    //
    private void setupChat() {
        //初始化chatArrayAdapter陣列
        chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //將陣列的值放進下方的listview
        lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);
    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        //若狀態為STATE_NONE，啟動ChatService
        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
    }

    //新增並初始化ListView
    private void getWidgetReferences() {
        lvMainChat = (ListView) findViewById(R.id.lvMainChat);
    }

    // 用此方法傳值給其他Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        //取得裝置的地址
        String address = data.getExtras().getString(DeviceList.DEVICE_ADDRESS);
        //將裝置地址存入device
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        //和裝置連接
        chatService.connect(device, secure);
    }

    //設定選單
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    //選擇功能(選單)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                serverIntent = new Intent(this, DeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
        }
        return false;
    }





    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        //設置圖表中曲線本身的樣式，包括顏色、點的大小以及線的粗細等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        r.setFillPoints(fill);
        r.setLineWidth(2);//這是線寬
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    //初始化圖表
    private void initChart(String xTitle,String yTitle,int minX,int maxX,int minY,int maxY){
        //這裡獲得main介面上的佈局，下面會把圖表畫在這個佈局裡面
        LinearLayout layout = (LinearLayout)findViewById(R.id.chart);
        //這個類用來放置曲線上的所有點，是一個點的集合，根據這些點畫出曲線
        series = new XYSeries("歷史曲線");
        //創建一個資料集的實例，這個資料集將被用來創建圖表
        mDataset = new XYMultipleSeriesDataset();
        //將點集添加到這個資料集中
        mDataset.addSeries(series);
        //以下都是曲線的樣式和屬性等等的設置，renderer相當於一個用來給圖表做渲染的控制碼
        int lineColor = Color.WHITE;
        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(lineColor, style, true);
        //設置好圖表的樣式
        setChartSettings(renderer, xTitle,yTitle,
                minX, maxX, //x軸最小最大值
                minY, maxY, //y軸最小最大值
                Color.WHITE, //坐標軸顏色
                Color.WHITE//標籤顏色
        );
        //生成圖表
        chart = ChartFactory.getLineChartView(this, mDataset, renderer);
        //將圖表添加到佈局中去
        layout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    //設定圖表一開始的格式
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        //有關對圖表的渲染可參看api文檔
        renderer.setChartTitle("");//設置標題
        renderer.setChartTitleTextSize(20);
        renderer.setXAxisMin(xMin);//設置x軸的起始點
        renderer.setXAxisMax(xMax);//設置一屏有多少個點
        renderer.setYAxisMin(50);
        renderer.setYAxisMax(150);
        renderer.setBackgroundColor(Color.BLACK);
        renderer.setLabelsColor(Color.YELLOW);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.BLUE);//設置格子的顏色
        renderer.setXLabels(10);//把x軸刻度平均分成多少個
        renderer.setYLabels(10);//把y軸刻度平均分成多少個
        renderer.setLabelsTextSize(25);
        renderer.setXTitle("TIME");//x軸的標題
        renderer.setYTitle("BPM");//y軸的標題
        renderer.setAxisTitleTextSize(30);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setPointSize((float) 2);
        renderer.setShowLegend(false);//說明文字
        renderer.setLegendTextSize(20);
        renderer.setZoomEnabled(false,false);
    }

    private int addX = -1;
    private double addY = 0;

    private void updateChart() {

        //設置好下一個需要增加的節點
        if(BPM>0 && BPM<1000){
            addX++;
            addY = BPM;//需要增加的值
        }
        //移除資料集中舊的點集
        mDataset.removeSeries(series);
        //判斷當前點集中到底有多少點，因為螢幕總共只能容納100個，所以當點數超過100時，長度永遠是100
        int length = 0;
        length = series.getItemCount();
        if (length > 5000) {//設置最多5000個數
            length = 5000;
        }
        series.add(addX, addY);//最重要的一句話，以xy對的方式往裡放值

        if(addX>xMax){
            renderer.setXAxisMin(addX-xMax);
            renderer.setXAxisMax(addX);
        }
        //重要：在資料集中添加新的點集
        mDataset.addSeries(series);
        //視圖更新，沒有這一步，曲線不會呈現動態
        //如果在非UI主執行緒中，需要調用postInvalidate()，具體參考api
        chart.invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //取消搜尋以提供程式更多的記憶體使用.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);


        //NOTE: Requires API 17+? JellyBean
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
        }
    }

    public void goback() {
        finish();
    }
}
