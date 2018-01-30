package com.example.shakti.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Switch onoffswitch;
    private static final int REQUEST_ENABLE_BT = 10;
    ListView lv1;
    ListView lv2;
    ArrayList pairednames =new ArrayList();
    ArrayList discoverednames = new ArrayList();
    BluetoothConnectionService mBluetoothConnection;
    private static final String TAG = "MainActivity";
    Button btnStartConnection;
    Button send;
    EditText et;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();


    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv1 = (ListView)findViewById(R.id.pDevices);
        lv2 = (ListView)findViewById(R.id.dDevices);
        onoffswitch= (Switch)findViewById(R.id.switch1);
        et=(EditText)findViewById(R.id.editText);
        send=(Button)findViewById(R.id.button2);

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        registerReceiver(mBroadcastReceiver3, new IntentFilter(BluetoothDevice.ACTION_FOUND));


        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported!!!", Toast.LENGTH_SHORT).show();
        }

        else if (!mBluetoothAdapter.isEnabled()) {
            onoffswitch.setChecked(false);
        }

        else if(mBluetoothAdapter.isEnabled())
        {
            onoffswitch.setChecked(true);
            printpaireddevices(mBluetoothAdapter);
            mBluetoothAdapter.startDiscovery();
        }

        onoffswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnonBluetooth();
                    Toast.makeText(getApplicationContext(), "above printpaireddevice", Toast.LENGTH_LONG).show();
                    printpaireddevices(mBluetoothAdapter);
                    mBluetoothAdapter.startDiscovery();
                    Toast.makeText(getApplicationContext(), "hi1", Toast.LENGTH_LONG).show();
                    //  printpaireddevices(mBluetoothAdapter);
                  //  printdiscoverdevices();
                } else {
                    turnoffBluetooth(mBluetoothAdapter);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = et.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
    }


    public void startConnection(View view){
        BluetoothDevice mBTDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("80:58:F8:15:BB:2F");
        startBtConnection(mBTDevice,MY_UUID_INSECURE);
    }

    public void startBtConnection(BluetoothDevice device,UUID uuid)
    {
        mBluetoothConnection.startClient(device,uuid);

    }


/*
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           // Toast.makeText(context, "Broadcast Received", Toast.LENGTH_LONG).show();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                discoverednames.add(deviceName);
                Toast.makeText(context, "BroadcastReceived", Toast.LENGTH_LONG).show();
                printdiscoverdevices(deviceName);
            }
        }
    };
*/
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                ArrayAdapter mDeviceListAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, mBTDevices);
                lv2.setAdapter(mDeviceListAdapter);
            }
        }
    };

    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver3);
        //mBluetoothAdapter.cancelDiscovery();
    }
    void turnonBluetooth()
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            }

    void turnoffBluetooth(BluetoothAdapter mBluetoothAdapter)
    {
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.disable();
        this.unregisterReceiver(mBroadcastReceiver3);
       // this.unregisterReceiver(mReceiver);
    }



    void printpaireddevices(BluetoothAdapter mBluetoothAdapter) {
        pairednames.clear();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Toast.makeText(this, " hello:"+pairedDevices.size(), Toast.LENGTH_SHORT).show();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                pairednames.add(deviceName);
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, pairednames);
            lv1.setAdapter(adapter);
        }
    }

    private void printdiscoverdevices(String deviceName) {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, discoverednames);
        lv2.setAdapter(adapter);
    }
}
