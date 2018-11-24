package com.example.yeet.ponephen;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName = "PHEN";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private class AcceptThread extends Thread {
        // the local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            //Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;
            //Blocking call and will only return on a successful connection or exception

            try {
                Log.d(TAG, "run: RFCOM server socket start......");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection.");
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }
            if (socket != null) {
                connected(socket, mmDevice);
            }
            Log.i(TAG, "END mAcceptThread.");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Cancelling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                try {
                    mmServerSocket.close();

                } catch (IOException e) {
                    Log.e(TAG, "cancel: Close of AcceptedThread ServerSocket failed." +
                            e.getMessage());
                }
            }
        }

        /*
        This thread runs while attempting to make an outgoing connection with a device. It
        runs straight through; the connection either succeeds or fails.
         */
        private class ConnectThread extends Thread {
            private BluetoothSocket mmSocket;

            public ConnectThread(BluetoothDevice device, UUID uuid) {
                Log.d(TAG, "ConnectThread: started.");
                deviceUUID = uuid;
            }

            public void run() {
                BluetoothSocket tmp = null;
                Log.i(TAG, "RUN mConnectThread ");
                //Get a BluetoothSocket for a connection with the given BluetoothDevice
                try {
                    Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " +
                            MY_UUID_INSECURE);
                    tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
                } catch (IOException e) {
                    Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket" + e.getMessage());
                }

                mmSocket = tmp;
                // Always cancel discovery  because it will slow down a connection

                mBluetoothAdapter.cancelDiscovery();

                //Make a connection to the BluetoothSocket

                //This is a blocking call and will only return on a successful connection or an exception
                try {
                    mmSocket.connect();
                    Log.d(TAG, "run: ConnectThread connected.");
                } catch (IOException e){
                    //close the socket
                    try{
                        mmSocket.close();
                        Log.d(TAG, "run: Closed Socket.");
                    } catch (IOException e1){
                        Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " +
                                e1.getMessage());
                    }
                    Log.d(TAG, "run: ConnectThread: Could not connect to UUID:" +MY_UUID_INSECURE);
                }
                connected(mmSocket, mmDevice);
        }
        public void cancel(){
                try{
                    Log.d(TAG, "cancel: Closing Client Socket.");
                    mmSocket.close();
                } catch (IOException e){
                    Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed."+e.getMessage());
                }
        }
        }

    }
}
