package better.life.autoquiet.receiver;

import static better.life.autoquiet.Sub.Sounds.utils;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import better.life.autoquiet.NotificationService;

public class BlueReceiver extends BroadcastReceiver {

    private static final String TAG = "Blue";

    public static final String QCY_PREFIX = "QCY";  // QCY crossky c30
    public static final String TESLA_PREFIX = "Tes";     // Your Tesla name prefix
    private static final String QCY_MAC = "84:AC:60:4F:60:16";  // LX-13 Mac

    // Corrected: Declare without immedi
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device == null) {
//            Logs.log(TAG, "Received Bluetooth event without a device object.");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED)
            return;
        String deviceName = device.getName();
        if (deviceName == null || deviceName.startsWith("Galaxy Fit"))
            return;
        if (deviceName.isEmpty())
            deviceName = getByMac(device.getAddress());

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            turnOnByName(deviceName);

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            turnOffByName(deviceName);
        }
    }
    private void turnOnByName(String deviceName) {
        boolean isInList = deviceName.startsWith(QCY_PREFIX) ||
                deviceName.startsWith(TESLA_PREFIX);

        if (isInList) {
            NotificationService.isBlueToothOn = true;
            NotificationService.blueDevice = deviceName;
//            Sounds.getInstance().setPlaybackVolume();
            utils.log(TAG, deviceName+ " 연결됨");
//        } else {
//            utils.log(TAG, "Ignoring for: " + deviceName);
        }
    }

    private void turnOffByName(String deviceName) {
        if (deviceName.startsWith(QCY_PREFIX)
                || deviceName.startsWith(TESLA_PREFIX)) {
            NotificationService.isBlueToothOn = false;
            NotificationService.blueDevice = "";
            utils.log(TAG, "해제 "+deviceName);
//        } else {
//            Logs.log(TAG, "Ignoring : " + deviceName);
        }
    }

    private String getByMac(String macAddress) {
        if (QCY_MAC.equals(macAddress)) {
            return QCY_PREFIX;
        }
        utils.log(TAG, "getByMac: " + macAddress);
        return "Unknown : "+macAddress;
    }
}