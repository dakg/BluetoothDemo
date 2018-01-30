package com.example.shakti.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class StartActivity extends AppCompatActivity {

    private static final String TAG="startactivity";
    private static final int ENABLE_REQUEST = 2;
    BluetoothAdapter mBluetoothAdapter;
    TextView status;
    TextView textView2;
    Switch enableBT;
    Button enableDis;

    Button showPairedDevices;
    Button showDiscoveredDevices;

    ArrayList<BluetoothDevice> paireDevices = new ArrayList<BluetoothDevice>();
    BluetoothDevice selectedDevice;

    ArrayList pairedNames = new ArrayList();
    ArrayList pairedAddress = new ArrayList();

    ListView plv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        status = (TextView)findViewById(R.id.statusd);
        textView2=(TextView)findViewById(R.id.textView2);
        enableBT =(Switch)findViewById(R.id.enableBT);
        showPairedDevices=(Button)findViewById(R.id.pd);
        showDiscoveredDevices=(Button)findViewById(R.id.dd);
        enableDis=(Button) findViewById(R.id.ed);
        plv = (ListView)findViewById(R.id.lvd);


        //to set initial status of switch
        setEnableBT();

        //Register Turn on/off receiver
        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, BTIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);

        enableBT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(enableBT.isChecked())
                {
                    turnOnBT();
                }
                else if(!enableBT.isChecked())
                {
                    turnOffBT();

                }
            }
        });

        enableDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverable);
            }
        });


        showPairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPairedDevices();
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
    }

    //to get list of paired devices
    private void getPairedDevices(){
        textView2.setText("Showing Paired Devices:");
        plv.setAdapter(null);
        pairedNames.clear();
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        //this.pairedDevices=pairedDevices;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                this.paireDevices.add(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                pairedNames.add(deviceName);
                pairedAddress.add(deviceHardwareAddress);
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, pairedNames);
            plv.setAdapter(adapter);
            plv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedDevice=(BluetoothDevice) paireDevices.get(i);
                    Toast.makeText(getApplicationContext(),"BluetoothDevice:" + selectedDevice.getName() + "selected" ,Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    // to set the status of switch
    private void setEnableBT()
    {
        if(mBluetoothAdapter.isEnabled())
        {
            enableBT.setChecked(true);
        //    enableBT.setText("Bluetooth On");
        }
        else if(!mBluetoothAdapter.isEnabled())
        {
            enableBT.setChecked(false);

        //    enableBT.setText("Bluetooth Off");
        }
    }

    private void turnOnBT()
    {
        if(mBluetoothAdapter == null){
            Toast.makeText(this,"Not Supported",Toast.LENGTH_SHORT).show();
        }
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,ENABLE_REQUEST);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ENABLE_REQUEST) {
            // Make sure the request was successful
           setEnableBT();
        }
    }
    private void turnOffBT()
    {
        mBluetoothAdapter.disable();
        plv.setAdapter(null);
    }


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        status.setText("Turned Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        status.setText("Turning Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        status.setText("Turned On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        status.setText("Turning On");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        Toast.makeText(getApplicationContext(),"mBroadcastReceiver2: Discoverability Enabled.",Toast.LENGTH_SHORT).show();

                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        Toast.makeText(getApplicationContext(),"mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        Toast.makeText(getApplicationContext(),"mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        Toast.makeText(getApplicationContext(),"mBroadcastReceiver2: Connecting....",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        Toast.makeText(getApplicationContext(),"mBroadcastReceiver2: Connected.",Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }
    };
}
