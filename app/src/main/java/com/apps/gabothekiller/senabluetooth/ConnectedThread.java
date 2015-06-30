package com.apps.gabothekiller.senabluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.Arrays;
//import java.util.ArrayList;

public class ConnectedThread extends Thread {
    private static final String TAG = "com.apps.gabothekiller.senabluetooth";
    private static final int MESSAGE_READ = 1;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;

    public ConnectedThread(BluetoothSocket socket,  Handler mHandler) {
        mmSocket = socket;
        this.mHandler = mHandler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { e.printStackTrace(); }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        // buffer store for the stream
        // bytes returned from read()
        int c = 0;
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {

                int bytes = 0;
                int future = mmInStream.available();
                c++;
                if ( future > 1) {
                    // Read from the InputStream

//                    String message;
                    byte[] buffer = new byte[0];
                    boolean notFirtsStop = true;
                    boolean notSecondStop = true;

//                    long futureTime = System.currentTimeMillis() + 500;
                    while ( notSecondStop  ){
                        future = mmInStream.available();
                        if (future > 1){
                            byte[] buff = new byte[ future ];

//                            Log.i(TAG, "IN RECEIVING LOOP " + c);
                            bytes = mmInStream.read(buff);
//                            Log.i(TAG, "END OF BLOCKING CALL " + c);


                            for (byte b : buff){
                                if ( b != 13) {
                                    byte[] gayArrayByte = {b};

                                    byte[] copy = new byte[buffer.length + 1];
                                    System.arraycopy(buffer, 0, copy, 0, buffer.length);
                                    System.arraycopy(gayArrayByte, 0, copy, buffer.length, 1);

                                    buffer = copy;
                                } else {
                                    if ( notFirtsStop ) {
                                        notFirtsStop = false;
                                    } else {
                                        notSecondStop  = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    mHandler.obtainMessage(MESSAGE_READ, bytes, c, buffer.clone() ).sendToTarget();
                } else {
                    SystemClock.sleep(100);
                }
            } catch (IOException e) {
                Log.i(TAG, "interrupted!");
                break;

            }
        }
    }

    public void changeThisFuckingHandlerOnceAndForAll (Handler mHandler){
        this.mHandler = mHandler;
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}