package com.apps.gabothekiller.senabluetooth;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class SecondActivity extends ActionBarActivity {
    public final static String TAG = "com.apps.gabothekiller.senabluetooth";
    private static final int MESSAGE_READ = 1;
    TextView gabosText;

    MyHandler handler;
    ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.i(TAG, "onCreate SecondActivity");


        // stuff which is gay
        gabosText = (TextView) findViewById(R.id.gabosText);
        String msm = "hola querido";
        gabosText.setText( msm );

        init();
    }


    private void init() {
        // create handler
        handler = new MyHandler(this);

        connectedThread = new ConnectedThread(MainActivity.btSocket, handler);
        connectedThread.write(  "Successfully Connected\n".getBytes()  );
        // start looking for messages
        connectedThread.start();
    }


    static class MyHandler extends Handler{
        private final WeakReference<SecondActivity> mTarget;
        SecondActivity mActivity;

        MyHandler(SecondActivity second) {
            mTarget = new WeakReference<>(second);
            mActivity = mTarget.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) msg.obj;

                    String s = "";
                    for( byte b : buffer){
                        s += Integer.toString( b ) + "-";
                    }

                    String message = new String(buffer);

                    Log.i(TAG, "buffer = " + message);

                    mActivity.gabosText.setText(s);

                    //Toast.makeText(mActivity, "MESSAGE RECEIVED", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_second, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
