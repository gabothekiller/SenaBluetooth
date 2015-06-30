package com.apps.gabothekiller.senabluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Handler;
import android.os.Message;

public class MainActivity extends Activity {
    private static final String TAG = "com.apps.gabothekiller.senabluetooth";
    private static final int SUCCESS_CONNECT = 0;
    ArrayAdapter<String> gabosAdapter;
    ListView gabosListView;
    //bluetooth
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevicesArray;
    ArrayList<BluetoothDevice> viewingDevices;
    // broadcast
    IntentFilter filter;
    BroadcastReceiver receiver;
    /* Array List of devices, this list will change by gabosAdapter, with the "add" method*/
    ArrayList<String> pairedDevicesStringArray;
    ArrayList<String> listDevices;

    static Handler mHandler;

    // this is the bluetooth connection to pass to other activities.
    //static BluetoothSocket btSocket;
    static ConnectedThread connectedThread;

    // connection between devices
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        if (btAdapter==null){
            Toast.makeText(this, "No bluetooth detected", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()){
                Toast.makeText(this, "Turn on bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }

            getPairedDevices();
            startDiscovery();
        }
    }

    private void init() {
        // initializing gabosListView
        pairedDevicesStringArray = new ArrayList<>();

        // list view own arrayList
        listDevices = new ArrayList<>();
        //listDevices.add("blow jobs");

        gabosAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listDevices);
        gabosListView = (ListView) findViewById(R.id.gabosListView);
        gabosListView.setAdapter(gabosAdapter);
        // setting up list view listener
        gabosListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String deviceText = String.valueOf(parent.getItemAtPosition(position));

                        if(btAdapter.isDiscovering()){
                            btAdapter.cancelDiscovery();
                        }

                        if( deviceText.contains("(Paired)") ){
                            // PAY ATTENTION TO THIS !!!!!!!!!!
                            BluetoothDevice selectedDevice = viewingDevices.get(position);
                            //ConnectThread connect = new ConnectThread(selectedDevice);
                            ConnectThread connect = new ConnectThread(selectedDevice, MY_UUID, btAdapter, mHandler);
                            connect.start();
                            //Toast.makeText(MainActivity.this, deviceText, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, deviceText + " is not pared", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // handler
        mHandler = new MyHandler(this);

        // bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // devices that are within area. this will be populated when discovering
        viewingDevices = new  ArrayList<>();

        // broadcast
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    viewingDevices.add(device);

                    String s = "";
                    for(int a = 0; a < pairedDevicesStringArray.size(); a++){
                        if(device.getName().equals(pairedDevicesStringArray.get(a))){
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
                    // Add the name and address to an array adapter to show in a ListView
                    gabosAdapter.add(device.getName()+"  "+s+'\n'+device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState() == BluetoothAdapter.STATE_OFF){
                        Toast.makeText(MainActivity.this, "You turned off bluetooth .l.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

            }
        };
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }


    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    /*
     * gets paired devices in system and stores in devicesArray and pairedDevices
     */
    private void getPairedDevices() {
        // array of devices set to the devices that are paired
        pairedDevicesArray = btAdapter.getBondedDevices();
        if (pairedDevicesArray.size() > 0){
            for(BluetoothDevice device : pairedDevicesArray){
                // do some code
                pairedDevicesStringArray.add(device.getName());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public static void setConnectedThread( BluetoothSocket socket, Handler handler ){
        connectedThread = new ConnectedThread(socket , handler);
        //MainActivity.btSocket = socket;
    }

    static class MyHandler extends Handler{
        //public final static String EXTRA_MESSAGE = "com.apps.gabothekiller.senabluetooth.MESSAGE";
        private final WeakReference<MainActivity> mTarget;
        MainActivity mActivity;




        MyHandler(MainActivity main) {
            mTarget = new WeakReference<>(main);
            mActivity = mTarget.get();

        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case SUCCESS_CONNECT:
                    Log.i(TAG, "in SUCCESS CONNECT");

                    // set the socket as static so that it can be accessed in other classes !!
                    BluetoothSocket mSocket = (BluetoothSocket) msg.obj;
                    setConnectedThread( mSocket, this );

                    Toast.makeText(mActivity, "CONNECT", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mActivity , SecondActivity.class);
                    mActivity.startActivity(intent);
                    break;
            }
        }
    }
}