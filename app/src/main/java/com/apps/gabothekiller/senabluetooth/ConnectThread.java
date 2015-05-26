package com.apps.gabothekiller.senabluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static final String TAG = "com.apps.gabothekiller.senabluetooth";
    private static final int SUCCESS_CONNECT = 0;
    private BluetoothSocket mmSocket;
    protected final BluetoothDevice mmDevice;
    BluetoothAdapter btAdapter;
    Handler mHandler;
    public UUID MY_UUID;


    public ConnectThread(BluetoothDevice device,UUID MY_UUID, BluetoothAdapter btAdapter, Handler mHandler) {
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        mmDevice = device;
        this.MY_UUID = MY_UUID;
        this.btAdapter = btAdapter;
        this.mHandler = mHandler;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();

        try {
            // MY_UUID is the app's UUID string, also used by the server code
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { e.printStackTrace(); }

        boolean isConnected = startConnection();

        if ( !isConnected) {
            try {
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) { e.printStackTrace(); }

            isConnected = startConnection();
            if (!isConnected){
                return;
            }
        }


        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        //Log.i(TAG, "Exiting RUN ");
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean startConnection(){
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            Log.i(TAG, "Connection Failed ");
            // Unable to connect; close the socket and get out
            try {
                //Log.i(TAG, "in Try block 2");
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return false;
        }
        Log.i(TAG, "Connection Successful ");
        return true;
    }
}
