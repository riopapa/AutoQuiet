package better.life.autoquiet.receiver;

import static better.life.autoquiet.Sub.Sounds.blueDevice;
import static better.life.autoquiet.Sub.Sounds.isBlueToothOn;
import static better.life.autoquiet.Sub.Sounds.utils;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class BlueReceiver extends BroadcastReceiver {

    private static final String TAG = "Blue";

    private static final String EARPHONE_PREFIX = "LX";  // LX-13
    private static final String TESLA_PREFIX = "Tes";     // Your Tesla name prefix

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) || BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (device == null) {
                utils.log(TAG, "Received Bluetooth event without a device object.");
                return;
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED)
                return;

            String deviceName = device.getName();
            if (deviceName == null) {
                utils.log(TAG, "Device connected, but name is currently null. MAC: " + device.getAddress());
                return;
            }

            isBlueToothOn = deviceName.startsWith(EARPHONE_PREFIX) || deviceName.startsWith(TESLA_PREFIX);

            if (isBlueToothOn) {
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    blueDevice = deviceName;
                    utils.log(TAG, "CONNECTED: " + deviceName);

                } else { // ACTION_ACL_DISCONNECTED
                    utils.log(TAG, "DisConnected " + blueDevice + " vs " + deviceName);
                    blueDevice = "";
                }
            } else {
                // It was a Bluetooth event, but not from a device we are interested in.
                utils.log(TAG, "Ignoring for: " + deviceName);
            }
        }
    }
}