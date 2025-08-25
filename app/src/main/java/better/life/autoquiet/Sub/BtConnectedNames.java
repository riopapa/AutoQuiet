package better.life.autoquiet.Sub;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class BtConnectedNames {


    public static List<String> getList(Context ctx) {
//        if (appCtx == null) return new ArrayList<>();

        BluetoothManager bm = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bm == null) return new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return new ArrayList<>();
        }

        int[] profiles = new int[] { // 7,8,1,2,21
                BluetoothProfile.HEADSET,
                BluetoothProfile.A2DP,
                BluetoothProfile.HEARING_AID,
                BluetoothProfile.GATT,
                BluetoothProfile.GATT_SERVER,
        };

        return getBtNames(profiles, bm, ctx);
    }

    private static @NonNull List<String> getBtNames(int[] profiles, BluetoothManager bm, Context ctx) {
        List <String> btNames = new ArrayList<>();

        for (int profile : profiles) {
            try {
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    return new ArrayList<>();
                }
                List<BluetoothDevice> list = bm.getConnectedDevices(profile);
                if (list == null) continue;
//                Log.w("ConnectedNames "+ profile, "list.size(): " + list.size());
                for (BluetoothDevice d : list) {
                    String name = d.getName();
//                    Log.w("ConnectedNames "+ profile, "name: " + name);
                    if (name != null && !name.trim().isEmpty()) {
                        btNames.add(name.trim());
                    }
                }
            } catch (Exception e) {
//                Log.e("ConnectedNames", profile + " Exception ");
            }
        }
        return btNames;
    }

    public static List<String> getConnectedBluetoothDeviceNames(Context context) {
        List<String> connectedDeviceNames = new ArrayList<>();
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Log.e("BluetoothChecker", "BluetoothManager is not available.");
            return connectedDeviceNames;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("BluetoothChecker", "Bluetooth is not supported or not enabled.");
            return connectedDeviceNames;
        }

        // List of common profiles to check.
        // It's crucial to handle exceptions for each profile as some might not be supported.
        int[] profiles = {
                BluetoothProfile.A2DP,
                BluetoothProfile.HEADSET,
                BluetoothProfile.GATT,
                BluetoothProfile.HEALTH
        };

        for (int profile : profiles) {
            try {
                // This call might throw an IllegalArgumentException if the profile is not supported
                List<BluetoothDevice> connectedDevices = bluetoothManager.getConnectedDevices(profile);
                for (BluetoothDevice device : connectedDevices) {
                    String deviceName = device.getName();
                    if (deviceName != null && !connectedDeviceNames.contains(deviceName)) {
                        connectedDeviceNames.add(deviceName);
                        Log.d("BluetoothChecker", "Found connected device for profile " + profile + ": " + deviceName);
                    }
                }
            } catch (IllegalArgumentException e) {
                // This is the key change. We catch the exception for this specific profile.
                Log.e("BluetoothChecker", "Profile not supported: " + profile, e);
                // Continue to the next profile in the loop
            } catch (SecurityException e) {
                // This handles the permission issue on Android 12+
                Log.e("BluetoothChecker", "Permission BLUETOOTH_CONNECT is required.", e);
                // No point in continuing if permissions are missing
                return connectedDeviceNames;
            }
        }

        return connectedDeviceNames;
    }

    public boolean isDeviceConnected(Context context, String deviceNameToCheck) {
        List<String> connectedDeviceNames = getConnectedBluetoothDeviceNames(context);
        return connectedDeviceNames.contains(deviceNameToCheck);
    }
}