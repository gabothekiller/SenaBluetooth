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
import java.util.ArrayList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class SecondActivity extends ActionBarActivity {
    public final static String TAG = "com.apps.gabothekiller.senabluetooth";
    private static final int MESSAGE_READ = 1;
    TextView gabosText;
    GraphView graph;
    ArrayList<Integer> data;
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>( new DataPoint[]{} );
    private int count = 0;

    MyHandler handler;
    ConnectedThread connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Log.i(TAG, "onCreate SecondActivity");

        init(savedInstanceState);
    }


    private void init(Bundle savedInstanceState) {
        // create handler
        handler = new MyHandler(this);
/*
        connectedThread = new ConnectedThread(MainActivity.btSocket, handler);
        connectedThread.write(  "Successfully Connected\n".getBytes()  );
        // start looking for messages
        connectedThread.start();*/
        connectedThread = MainActivity.connectedThread;
        connectedThread.changeThisFuckingHandlerOnceAndForAll(handler);
        connectedThread.write(  "Successfully Connected\n".getBytes()  );
        // start looking for messages


        // stuff which is gay
        gabosText = (TextView) findViewById(R.id.gabosText);
        String msm = "hola querido";
        gabosText.setText( msm );

        makeGraph();

        if(savedInstanceState != null) {
            savedDataSeries( savedInstanceState );
        } else {
            data = new ArrayList<>();
            connectedThread.start();
        }
    }
    private void savedDataSeries(Bundle savedInstanceState){
        data = savedInstanceState.getIntegerArrayList("savedDataSeries");

        for (int i = 0, length = data.size() ; i < length ; i+=2) {
            series.appendData(new DataPoint(data.get(i), data.get(i+1) ), true, 9999);
        }
        if (data.size() != 0){
            count = data.get( data.size() - 2 );
        }
    }

    private void makeGraph(){
        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Unintentional title!");

        graph.getViewport().setScalable(true);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("x");
        // this stuff, puffff!!!
        graph.getGridLabelRenderer().setNumVerticalLabels( 6 );
        graph.getGridLabelRenderer().setNumHorizontalLabels( 8 );

        // Y axis
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(255);

        // X axis
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(50);

        graph.addSeries(series);
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
                    /*
                    for( byte b : buffer){
                        s += Integer.toString( b ) + "-";
                        mActivity.series.appendData(  new DataPoint(mActivity.count++, b), true, 9999   );

                        //mActivity.graph.getViewport().setMaxX( mActivity.count );
                    }*/


                    for( int i = 0; i < buffer.length ; i+=2){


                        int number;
                        byte a = buffer[i];
                        byte b;
                        try {
                            b = buffer[i + 1];
                        } catch (IndexOutOfBoundsException e){
                            b = buffer[i];
                        }
                        if (a != 10) {
                            a = converter(a);
                            b = converter(b);
                            number = a * 16 + b;
                            mActivity.series.appendData(  new DataPoint(mActivity.count++, number), true, 9999   );
                            mActivity.data.add( mActivity.count );
                            mActivity.data.add( number );
                            s += Integer.toString(number) + "-";
                        }
                    }
                    String message = new String(buffer);
                    Log.i(TAG, "buffer = " + message);
                    mActivity.gabosText.setText( s );

                    //Toast.makeText(mActivity, "MESSAGE RECEIVED", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private static byte converter ( byte n){
            if (48 <= n && n <= 57){
                n -= 48;
            }else if ( 65 <= n && n <= 70 ){
                n -= 65;
                n += 10;
            }
            return n;
        }

    }
    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState){
        savedInstanceState.putIntegerArrayList("savedDataSeries", data);
        // LineGraphSeries<Datapoint> series
        super.onSaveInstanceState(savedInstanceState);
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
