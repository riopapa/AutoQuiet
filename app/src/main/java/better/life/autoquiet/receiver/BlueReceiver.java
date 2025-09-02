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
import android.os.Handler;
import android.os.Looper;

public class BlueReceiver extends BroadcastReceiver {

    private static final String TAG = "Bluetooth";

    private static final String EARPHONE_PREFIX = "QCY";  // LX-13
    private static final String EARPHONE_MAC = "09:A8:14:88:82:6A";  // LX-13 Mac
    private static final String TESLA_PREFIX = "Tes";     // Your Tesla name prefix
    private static final long DELAY_MILLIS = 500; // 500 ms delay to get the device name

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device == null) {
            utils.log(TAG, "No device object.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED)
            return;

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            String deviceName = device.getName();

            if (deviceName == null || deviceName.isEmpty()) {
                utils.log(TAG, "Device connected, but name is currently null. MAC: " + device.getAddress() + ". Retrying in " + DELAY_MILLIS + "ms.");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Re-check permissions inside the delayed runnable
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED)
                        return;
                    String delayedName = device.getName();
                    if (delayedName == null || delayedName.isEmpty()) {
                        delayedName = getFallbackName(device.getAddress());
                    }
                    handleConnection(context, device, delayedName, true);
                }, DELAY_MILLIS);
            } else {
                handleConnection(context, device, deviceName, false);
            }

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            String deviceName = device.getName();

            if (deviceName == null || deviceName.isEmpty()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED)
                        return;
                    String delayedName = device.getName();
                    if (delayedName == null || delayedName.isEmpty()) {
                        delayedName = getFallbackName(device.getAddress());
                    }
                    handleDisconnection(delayedName, true);
                }, DELAY_MILLIS);
            } else {
                handleDisconnection(deviceName, false);
            }
        }
    }

    private String getFallbackName(String macAddress) {
        if (EARPHONE_MAC.equals(macAddress)) {
            return EARPHONE_PREFIX;
        } else {
            // Add more fallback logic here for other devices
            return "Unknown Device";
        }
    }

    private void handleConnection(Context context, BluetoothDevice device, String deviceName, boolean isDelayed) {
        isBlueToothOn = deviceName.startsWith(EARPHONE_PREFIX) || deviceName.startsWith(TESLA_PREFIX);

        if (isBlueToothOn) {
            blueDevice = deviceName;
            utils.log(TAG, "CONN " + deviceName + (isDelayed ? " (delayed)" : ""));
        } else {
            utils.log(TAG, "Ignoring for: " + deviceName);
        }
    }

    private void handleDisconnection(String deviceName, boolean isDelayed) {
        if (deviceName.startsWith(EARPHONE_PREFIX) || deviceName.startsWith(TESLA_PREFIX)) {
            utils.log(TAG, "curr:" + blueDevice + " vs disCon" + deviceName + (isDelayed ? " (delayed)" : ""));
            blueDevice = "";
            isBlueToothOn = false;
        } else {
            utils.log(TAG, "Ignoring disconnection for: " + deviceName);
        }
    }
}