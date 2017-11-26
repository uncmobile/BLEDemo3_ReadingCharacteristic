package com.example.nirjon.bledemo3_readingcharacteristic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    BluetoothManager myManager;
    BluetoothAdapter myAdapter;
    BluetoothLeScanner myScanner;
    BluetoothDevice myDevice;

    BluetoothGatt myGatt;
    BluetoothGattService myService;
    BluetoothGattCharacteristic myCharacteristics;
    BluetoothGattDescriptor myDescriptor;

    BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            for(BluetoothGattService s : gatt.getServices()){
                for(BluetoothGattCharacteristic c: s.getCharacteristics()){
                    Log.i("Tag", "Service: " + s.getUuid() + ", Characteristic: " + c.getUuid());
                    if(c.getUuid().toString().equals("00002a37-0000-1000-8000-00805f9b34fb")){
                        Log.i("Tag", "This is the UUID of globally known heart rate characteristics.");
                        Log.i("Tag", "Saving the characteristics for later use.");
                        myCharacteristics = c;
                    }
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("Tag", "Heart Rate: " + characteristic.getValue()[1]);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i("Tag", "Writing descriptor");
        }
    };

    ScanCallback myScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result.getDevice().getName() != null){
                myDevice = result.getDevice();
                if(myDevice.getName().equals("MIO GLOBAL")){
                    Log.i("Tag", "That's MIO smartwatch!");
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startScanButton(View v) {
        myManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        myAdapter = myManager.getAdapter();
        myScanner = myAdapter.getBluetoothLeScanner();
        myScanner.startScan(myScanCallback);
    }

    public void stopScanAndListCharacteristicsButton(View v) {
        myScanner.stopScan(myScanCallback);
        myGatt = myDevice.connectGatt(getApplicationContext(), false, myGattCallback);
    }

    public void readCharacteristicsButton(View v){
        myGatt.setCharacteristicNotification(myCharacteristics, true);
        myDescriptor = myCharacteristics.getDescriptors().get(0);
        myDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        myGatt.writeDescriptor(myDescriptor);
    }

    public void disconnectButton(View v){
        myGatt.disconnect();
    }
}
