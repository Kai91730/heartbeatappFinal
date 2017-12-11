package com.example.user.heartbeatapp;


import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ChatService {
    private static final String NAME_SECURE = "BluetoothChatSecure";

    // 獨立的 UUID
    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  //手機連HC-05

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread secureAcceptThread;
    public ConnectThread connectThread;
    public ConnectedThread connectedThread;
    private int state;

    // 表示連線狀態的常數
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1; // 監聽連線
    public static final int STATE_CONNECTING = 2; // 連線中
    public static final int STATE_CONNECTED = 3; // 連接到裝置

    public ChatService(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;

        this.handler = handler;
    }

    // 設置目前連線的狀態
    private synchronized void setState(int state) {
        this.state = state;

        handler.obtainMessage(SettingActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // 取得目前連線的狀態
    public synchronized int getState() {
        return state;
    }

    // 啟動ChatService
    public synchronized void start() {
        // 取消任何執行緒
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // 取消進行中的執行緒
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);

        // 啟動BluetoothServerSocket上的執行緒用以監聽
        if (secureAcceptThread == null) {
            secureAcceptThread = new AcceptThread(true);
            secureAcceptThread.start();
        }
    }

    // 初始化連線以連接裝置
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        // 取消任何執行緒
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // 取消進行中的執行緒
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // 從取得的裝置清單中連接
        connectThread = new ConnectThread(device, secure);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    // 管理藍芽連線
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device, final String socketType) {
        // 取消任何執行緒
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // 取消進行中的執行緒
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        // 啟動執行緒以管理連線及實作傳輸
        connectedThread = new ConnectedThread(socket, socketType);
        connectedThread.start();

        // 回傳連線中的裝置名稱給 UI Activity
        Message msg = handler.obtainMessage(SettingActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(SettingActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    //連線失敗
    private void connectionFailed() {
        Message msg = handler.obtainMessage(SettingActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(SettingActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // 重啟至監聽模式
        ChatService.this.start();
    }

    //失去連線
    private void connectionLost() {
        Message msg = handler.obtainMessage(SettingActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(SettingActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // 重啟至監聽模式
        ChatService.this.start();
    }

    // 監聽裝置時執行
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private String socketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            socketType = "Secure";

            try {
                if (secure) {
                    //監聽用戶的連接請求
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                            NAME_SECURE, MY_UUID_SECURE);
                }
            } catch (IOException e) {
            }
            serverSocket = tmp;
        }

        public void run() {
            setName("AcceptThread" + socketType);

            BluetoothSocket socket = null;

            while (state != STATE_CONNECTED) {
                try {
                    //取得和Client端連線的Socket物件
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // 若連線被接受
                if (socket != null) {
                    synchronized (ChatService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // 執行 connected thread
                                connected(socket, socket.getRemoteDevice(),
                                        socketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // 嘗試向外連線時啟動
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private String socketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            this.device = device;
            BluetoothSocket tmp = null;
            socketType = "Secure";

            try {
                if (secure) {
                    tmp = device
                            //根據UUID創建並返回一個BluetoothSocket
                            .createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                }
            } catch (IOException e) {
            }
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread" + socketType);

            // 為了不降低連線速度所以取消搜尋裝置
            bluetoothAdapter.cancelDiscovery();

            // 連線到 BluetoothSocket
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // 完成連線時重設ConnectThread
            synchronized (ChatService.this) {
                connectThread = null;
            }

            // 啟動 connected thread
            connected(socket, device, socketType);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    // 連接裝置時啟動
    public class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;

        public byte[] buffer = new byte[1024];
        public int bytes = 0;
        public ConnectedThread(BluetoothSocket socket, String socketType) {
            this.bluetoothSocket = socket;
            InputStream tmpIn = null;


            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
            }

            inputStream = tmpIn;

        }

        public void run() {


            // 持續監聽 InputStream
            while (true) {
                try {
                    // 從InputStream讀取
                    bytes = inputStream.read(buffer);
                    // 讀取Arduino傳來的值

                    handler.obtainMessage(SettingActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    connectionLost();
                    // 重啟至監聽模式
                    ChatService.this.start();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
            }
        }
    }
}