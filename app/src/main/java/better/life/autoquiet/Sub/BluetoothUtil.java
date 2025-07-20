package better.life.autoquiet.Sub;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";
    private static final String EARPHONE = "联";    // earphone
    private static final String TESLA = "Tes";  // tesla
    private static final int PROFILE_PROXY_TIMEOUT_MS = 5000; // Timeout for getting profile proxy

    public static String getDevice(Context context) {
        // Check for necessary BLUETOOTH_CONNECT permission for SDK 31+
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted. Request this permission at runtime.");
            // You should request the permission from the user before calling this.
            // Example: ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT);
            return "";
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "BluetoothManager is not available.");
            return "";
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "BluetoothAdapter is not available.");
            return "";
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is not enabled.");
            return "";
        }

        // Use a CountDownLatch to wait for the profile proxy to be connected
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BluetoothHeadset> headsetProxy = new AtomicReference<>();
        AtomicReference<String> connectedDeviceName = new AtomicReference<>();

        BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
//                Log.w("Connected", profile + " " + proxy);
                if (profile == BluetoothProfile.HEADSET) {
                    headsetProxy.set((BluetoothHeadset) proxy);
//                    Log.w(TAG, "BluetoothHeadset profile proxy connected.");

                    // Check for BLUETOOTH_CONNECT permission again before accessing connected devices
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                        Log.w(TAG, "BLUETOOTH_CONNECT permission lost or not granted after proxy connection.");
                        latch.countDown(); // Release latch even on permission issue
                        return;
                    }

                    List<BluetoothDevice> connectedDevices = headsetProxy.get().getConnectedDevices();
                    if (connectedDevices != null) {
                        for (BluetoothDevice device : connectedDevices) {
                            // Check for BLUETOOTH_CONNECT permission again before accessing device name
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                                Log.w(TAG, "BLUETOOTH_CONNECT permission lost or not granted while getting device name.");
                                break; // Stop processing devices
                            }
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//                                Log.w(TAG, "BLUETOOTH permission lost or not granted for older SDK while getting device name.");
                                break; // Stop processing devices
                            }

                            String deviceName = device.getName();
                            if (deviceName != null && (deviceName.startsWith(EARPHONE)
                                    || deviceName.startsWith(TESLA))) {
                                connectedDeviceName.set(deviceName);
//                                Log.w(TAG, "Found connected target device: " + deviceName);
                                break; // Found a match, no need to continue
                            }
                        }
                    }
                    latch.countDown(); // Signal that we have checked the connected devices
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HEADSET) {
                    headsetProxy.set(null);
//                    Log.d(TAG, "BluetoothHeadset profile proxy disconnected.");
                    latch.countDown(); // Release latch on disconnection as well
                }
            }
        };

        // Establish connection to the profile proxy. This is asynchronous.
        // Requires BLUETOOTH_CONNECT permission on API 31+
        boolean success = bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET);

        if (!success) {
            Log.e(TAG, "Failed to get BluetoothHeadset profile proxy. Check permissions and Bluetooth state.");
            return "";
        }

        try {
            // Wait for the profile proxy to be connected and checked
            latch.await(PROFILE_PROXY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for Bluetooth profile proxy.", e);
            Thread.currentThread().interrupt();
        } finally {
            // Close the profile proxy to release resources
            BluetoothHeadset proxy = headsetProxy.get();
            if (proxy != null) {
                // Closing the profile proxy also requires BLUETOOTH_CONNECT permission
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy);
//                    Log.w(TAG, "BluetoothHeadset profile proxy closed.");
                } else {
                    Log.w(TAG, "BLUETOOTH_CONNECT permission not granted to close profile proxy.");
                }
            }
        }
//        Log.w("BluetoothUtil", "getConnectedTargetDeviceName: " + connectedDeviceName.get());
        String deviceName = connectedDeviceName.get();
        if (deviceName != null)
            return deviceName;
        return "";
    }
}