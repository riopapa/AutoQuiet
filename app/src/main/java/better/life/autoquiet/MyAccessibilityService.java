package better.life.autoquiet;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyAccessibilityService extends AccessibilityService {

    // Interface for callback to deliver touch coordinates
    public interface OnTouchPositionListener {
        void onTouch(int x, int y);
    }

    private WindowManager windowManager;

    public static MyAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Log.w("MyService", "AccessibilityService connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Optional: you can handle events here
    }

    @Override
    public void onInterrupt() {
        // Required override for AccessibilityService
    }

    public void performClick(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 100);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();

        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(@Nullable GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
//                DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
//                int screenWidth = displayMetrics.widthPixels;
//                int screenHeight = displayMetrics.heightPixels;
//
//                Log.w("MyService", "Click performed at (" + x + ", " + y + ") (" + screenWidth + ", " + screenHeight + ")");
            }

            @Override
            public void onCancelled(@Nullable GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w("MyService", "Click cancelled");
            }
        }, null);
    }

    public void inputText(String text) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        AccessibilityNodeInfo editableNode = findEditableNode(rootNode);
        if (editableNode == null) return;

        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    private AccessibilityNodeInfo findEditableNode(AccessibilityNodeInfo node) {
        if (node == null) return null;
        if (node.isEditable()) return node;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            AccessibilityNodeInfo result = findEditableNode(child);
            if (result != null) return result;
        }
        return null;
    }

    public void movePointer(float begX, float begY, float endX, float endY, long durationMs, @Nullable Runnable onComplete) {
        Path path = new Path();
        path.moveTo(begX, begY);
        path.lineTo(endX, endY);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, durationMs);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();

        dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(@Nullable GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onCancelled(@Nullable GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.w("Gesture", "Canceled: from=(" + begX + ", " + begY + ") to=(" + endX + ", " + endY + ")");
            }
        }, null);
    }

    /**
     * Shows a fullscreen transparent overlay that listens for a single touch.
     * The touch coordinates are delivered via the callback interface.
     * After touch, the overlay is removed automatically.
     */
    public void getCurrPos(@NonNull final OnTouchPositionListener callback) {
        if (windowManager == null) {
            Log.e("MyService", "WindowManager is null! Cannot create overlay.");
            return;
        }

        final View overlayView = new View(this);
        overlayView.setBackgroundColor(0x00000000); // Fully transparent
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getRawX();
                    int y = (int) event.getRawY();
                    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
                    int screenWidth = displayMetrics.widthPixels;
                    int screenHeight = displayMetrics.heightPixels;
                    Log.w("overlayView", "onTouch at (" + x + ", " + y + ") (" + screenWidth + ", " + screenHeight + ")");

                    callback.onTouch(x, y);

                    try {
                        windowManager.removeView(overlayView);
                    } catch (Exception e) {
                        Log.e("MyService", "Failed to remove overlay", e);
                    }

                    return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(overlayView, params);
            Log.d("MyService", "Temporary overlay added. Waiting for touch.");
        } catch (Exception e) {
            Log.e("MyService", "Error adding overlay", e);
        }
    }
}
