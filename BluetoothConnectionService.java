package com.example.shakti.bluetoothdemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by shakti on 29-01-2018.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appname = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter  mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context mContext) {
        this.mContext = mContext;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread()
        {
            BluetoothServerSocket temp=null;
            try {
                temp= mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appname,MY_UUID_INSECURE);
                Log.d(TAG,"Setting up server using :"+ MY_UUID_INSECURE);

            } catch (IOException e) {
                Log.d(TAG,"AcceptThread : IoException:"+ e.getMessage());
            }
            mServerSocket=temp;
        }
        public void run(){
            Log.d(TAG,"run : Accept Thread Running.");
            BluetoothSocket socket =null;
            try {
                Log.d(TAG,"run : RFcomm Server Socket started...");
                socket=mServerSocket.accept();
                Log.d(TAG,"run : Server socket accepted");

            } catch (IOException e) {
                Log.d(TAG,"AcceptThread : IoException:"+ e.getMessage());
            }

            if(socket!=null)
            {
                connected(socket,mmDevice);

            }
            Log.i(TAG,"End Accepted Thread");
        }

        public void cancel() {
            Log.d(TAG,"cancel: Cancelling Accepted Thread");
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"cancel: Cancelling Accepted Thread Falied" + e.getMessage());
            }
        }

    }


    private class ConnectThread extends Thread{
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device , UUID uuid) {
            Log.d(TAG,"Connect thread started");
            mmDevice=device;
            deviceUUID = uuid;
        }
        public void run()
        {
            BluetoothSocket tmp = null;
            Log.i(TAG,"Run mConnectThread");
            try {
                Log.d(TAG,"Trying to create Rfcomm channel");
                tmp=mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d(TAG,"Connect thread: IOexception" + e.getMessage());
            }

            mSocket=tmp;
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                Log.d(TAG,"Connect thread: Connected Successfully " );

            } catch (IOException e) {
                try {
                    mSocket.close();
                    Log.d(TAG,"Connect thread: Connection failed" );
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            connected(mSocket,mmDevice);
        }
        public void cancel() {
          //  Log.d(TAG,"cancel: Cancelling Accepted Thread");
            try {
                mSocket.close();
            } catch (IOException e) {
       //         Log.e(TAG,"cancel: Cancelling Accepted Thread Falied" + e.getMessage());
            }
        }
    }



    public synchronized void start(){
        Log.d(TAG,"Start");
        if(mConnectThread!=null)
        {
            mConnectThread.cancel();
            mConnectThread=null;
        }

        if(mInsecureAcceptThread==null)
        {
            mInsecureAcceptThread= new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device ,UUID uuid)
    {
        Log.d(TAG,"Start client started");
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please wait",true);
        mConnectThread=new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    public class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)  {
            Log.d(TAG,"Connected Thread Starting");
            mmSocket=socket;
            InputStream tmpIn=null;
            OutputStream tmpOut=null;
            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            try {
                tmpIn=mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream=tmpIn;
            mmOutStream=tmpOut;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            while(true)
            {
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG,"InputStream:" + incomingMessage);
                } catch (IOException e) {
                    Log.d(TAG,"Error reading from inputstream"+ e.getMessage());
                    break;
                }
            }
        }
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"Wrting OutputStream:" + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG,"Error writing to outputstream"+ e.getMessage());
            }
        }
        public void cancel() {
            //  Log.d(TAG,"cancel: Cancelling Accepted Thread");
            try {
                mmSocket.close();
            } catch (IOException e) {
                //         Log.e(TAG,"cancel: Cancelling Accepted Thread Falied" + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mSocket, BluetoothDevice mmDevice) {
        Log.d(TAG,"Connected Method Started");
        mConnectedThread=new ConnectedThread(mSocket);
        mConnectedThread.start();
    }
    public void write(byte[] out)
    {
        ConnectedThread r;
        mConnectedThread.write(out);
    }
}
