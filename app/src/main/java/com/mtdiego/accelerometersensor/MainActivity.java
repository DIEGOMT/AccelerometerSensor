package com.mtdiego.accelerometersensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private Sensor CamBienGiaToc;
    private SensorManager sensorManager;
    private Button bON, bOFF, bListDevices, bListen, send;
    private TextView tx,ty,tz, ttoi, tlui, ttrai, tphai, status, msg_box;
    private ListView listView;
    private EditText writeMsg;
    private float x,y,z;

    public BluetoothAdapter myBluetoothAdapter;
    public Intent enablingIntent;
    public static final int requestCodeForEnable = 1;
    //--------------------------------------------------------
    ArrayList<String> stringArrayList = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    BluetoothDevice[] btArray;
    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;

    private static final String APP_NAME = "Accelerometer Sensor";
    private static final UUID MY_UUID=UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tao Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Lay Cam bien gia toc
        CamBienGiaToc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Dang ky su dung Sensor
        sensorManager.registerListener(this, CamBienGiaToc, SensorManager.SENSOR_DELAY_NORMAL);

        tx          = findViewById(R.id.textView_x);
        ty          = findViewById(R.id.textView_y);
        tz          = findViewById(R.id.textView_z);
        ttoi        = findViewById(R.id.textView_toi);
        tlui        = findViewById(R.id.textView_lui);
        ttrai       = findViewById(R.id.textView_trai);
        tphai       = findViewById(R.id.textView_phai);
        bON         = findViewById(R.id.button_on);
        bOFF        = findViewById(R.id.button_off);
        bListDevices= findViewById(R.id.button_listDevices);
        listView    = findViewById(R.id.listView);
        bListDevices= findViewById(R.id.button_listDevices);
        status      = findViewById(R.id.textView_status);
        msg_box     = findViewById(R.id.textView_msg_box);
        bListen     = findViewById(R.id.button_listen);
        send        = findViewById(R.id.button_send);
        writeMsg    = findViewById(R.id.editText_type);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        //-------------------------------------------------------------------------
        bluetoothONMethod();
        bluetoothOFFMethod();
        //-------------------------------------------------------------------------

        bListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });

        bListDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt=myBluetoothAdapter.getBondedDevices();
                String[] strings=new String[bt.size()];
                btArray=new BluetoothDevice[bt.size()];
                int index=0;

                if( bt.size()>0)
                {
                    for(BluetoothDevice device : bt)
                    {
                        btArray[index]= device;
                        strings[index]=device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass=new ClientClass(btArray[i]);
                clientClass.start();

                status.setText("Connecting...");
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string= String.valueOf(writeMsg.getText());
                sendReceive.write(string.getBytes());
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        tx.setText("x: " + x);
        ty.setText("y: " + y);
        tz.setText("z: " + z);

        Resources res = getResources();
        int color_red = res.getColor(R.color.Red);
        int color_gray= res.getColor(R.color.Gray);

        if(z > 8 && (z > 3 && y > -2 && y < 4)) {
            ttoi.setTextColor(color_red);
            tlui.setTextColor(color_gray);
            ttrai.setTextColor(color_gray);
            tphai.setTextColor(color_gray);
        }
        if(z < 3 && (z < 8 && y > -2 && y < 4)) {
            tlui.setTextColor(color_red);
            ttoi.setTextColor(color_gray);
            ttrai.setTextColor(color_gray);
            tphai.setTextColor(color_gray);
        }
        if(y < -2&& (z < 8 && z > 3  && y < 4)){
            ttrai.setTextColor(color_red);
            ttoi.setTextColor(color_gray);
            tlui.setTextColor(color_gray);
            tphai.setTextColor(color_gray);
        }
        if(y > 4 && (z < 8 && z > 3  && y >-2)) {
            tphai.setTextColor(color_red);
            ttoi.setTextColor(color_gray);
            tlui.setTextColor(color_gray);
            ttrai.setTextColor(color_gray);
        }
        //---
        if((z > 8 && y < -2) && (z >= 3 && y <= 4)){
            ttoi.setTextColor(color_red);
            ttrai.setTextColor(color_red);
            tlui.setTextColor(color_gray);
            tphai.setTextColor(color_gray);
        }
        if((z >8 && y > 4) && (z >= 3 && y >=-2)){
            ttoi.setTextColor(color_red);
            tphai.setTextColor(color_red);
            tlui.setTextColor(color_gray);
            ttrai.setTextColor(color_gray);
        }
        if((z < 3 && y < -2) && (z <= 8 && y <=4)){
            tlui.setTextColor(color_red);
            ttrai.setTextColor(color_red);
            ttoi.setTextColor(color_gray);
            tphai.setTextColor(color_gray);
        }
        if((z < 3 && y > 4) && (z <= 8 && y >=-2)){
            tlui.setTextColor(color_red);
            tphai.setTextColor(color_red);
            ttoi.setTextColor(color_gray);
            ttrai.setTextColor(color_gray);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    //-----------------------------------------------------------------------------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == requestCodeForEnable){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(), "Bluetooth is Enabled", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Bluetooth enabling Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void bluetoothONMethod() {
        bON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter == null){
                    Toast.makeText(getApplicationContext(), "Bluetooth doesn't support on your device.", Toast.LENGTH_LONG).show();
                }
                else{
                    if(!myBluetoothAdapter.isEnabled()){
                        startActivityForResult(enablingIntent, requestCodeForEnable);
                    }
                    else if (myBluetoothAdapter.isEnabled())
                        Toast.makeText(getApplicationContext(),"Bluetooth already Enabled.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bluetoothOFFMethod() {
        bOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myBluetoothAdapter.isEnabled()){
                    myBluetoothAdapter.disable();
                    Toast.makeText(getApplicationContext(),"Bluetooth is Disabled!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //-------------------------------------------------------------------------------

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket=myBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;

            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
